package com.ddapp.my.ddqrcode.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ddapp.my.ddqrcode.R;
import com.ddapp.my.ddqrcode.decode.camera.CameraManager;
import com.ddapp.my.ddqrcode.decode.decoding.CaptureActivityHandler;
import com.ddapp.my.ddqrcode.decode.decoding.IDecodeView;
import com.ddapp.my.ddqrcode.decode.decoding.InactivityTimer;
import com.ddapp.my.ddqrcode.decode.view.ViewfinderView;
import com.ddapp.my.ddqrcode.image.RGBLuminanceSource;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class CaptureActivity extends Activity implements Callback, OnClickListener, IDecodeView {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    private static final int REQUEST_CODE = 100;
    private static final int PARSE_BARCODE_SUC = 300;
    private static final int PARSE_BARCODE_FAIL = 303;
    private ProgressDialog mProgress;
    private String photo_path;
    private Bitmap scanBitmap;
    private Result result;
    private Result tempResult;
    private String className;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_capture);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        initView();

        CameraManager.init(getApplicationContext());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

    }

    private void initView() {
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinderView);
        RelativeLayout title = (RelativeLayout) this.findViewById(R.id.qrscan_title);
        // TextView titleView = (TextView)
        // title.findViewById(R.id.textview_title);
        // titleView.setText("二维码扫描");
/*		RelativeLayout.LayoutParams menuPara = (RelativeLayout.LayoutParams) title.getLayoutParams();
        menuPara.topMargin = ScreenUtil.to320(20);
        title.setLayoutParams(menuPara);*/
        LinearLayout toBack = (LinearLayout) title.findViewById(R.id.back_layout);
        LinearLayout openAblum = (LinearLayout) title.findViewById(R.id.button_openAlbum);
        toBack.setOnClickListener(this);
        openAblum.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet, this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }


    public void handleDecode(Result obj, Bitmap barcode) {
        inactivityTimer.onActivity();
//        viewfinderView.drawResultBitmap(barcode);
        playBeepSoundAndVibrate();

        Intent it = new Intent();
        Uri uri = Uri.parse("duanshu://com.duanshu.h5.mobile/browser");
        it.setData(uri);
        it.putExtra("url",obj.getText());
        it.setAction(Intent.ACTION_VIEW);
        if (it.resolveActivity(getBaseContext().getPackageManager()) != null) {
            CaptureActivity.this.startActivity(it);
            CaptureActivity.this.finish();
        }


    }

    private String getResult(String resultString) {
        if (resultString.equals("")) {
            finish();
            resultString = getString(R.string.dingdone_string_194);
        } else {
            JSONObject jsonObject;
            ParsedResultType type = ResultParser.parseResult(tempResult).getType();
            if (type == ParsedResultType.URI) {
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("uri", resultString);
                    resultString = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (type == ParsedResultType.EMAIL_ADDRESS) {
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("address_email", resultString);
                    resultString = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (type == ParsedResultType.ADDRESSBOOK) {
                try {
                    jsonObject = new JSONObject();
                    JSONObject object = new JSONObject();
                    AddressBookParsedResult result = (AddressBookParsedResult) ResultParser.parseResult(tempResult);
                    if (result != null) {

                        if (result.getNames() != null) {
                            object.put("names", result.getNames()[0]);
                        }

                        if (result.getPhoneNumbers() != null) {
                            object.put("phoneNumbers", result.getPhoneNumbers()[0]);
                        }

                        if (result.getAddresses() != null) {
                            object.put("addresses", result.getAddresses()[0]);
                        }

                        if (result.getEmails() != null) {
                            object.put("emails", result.getEmails()[0]);
                        }

                        if (!TextUtils.isEmpty(result.getOrg())) {
                            object.put("org", result.getOrg());
                        }

                        if (result.getURLs() != null) {
                            object.put("url", result.getURLs()[0]);
                        }

                        if (!TextUtils.isEmpty(result.getNote())) {
                            object.put("note", result.getNote());
                        }
                    }

                    JSONArray resultArray = new JSONArray();
                    resultArray.put(object);
                    jsonObject.put("address_book", resultArray);
                    resultString = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "";
                }

            }
        }
        return resultString;
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mProgress.dismiss();
            switch (msg.what) {
                case PARSE_BARCODE_SUC:

                    handleDecode(result, scanBitmap);

                    break;
                case PARSE_BARCODE_FAIL:
                    Toast.makeText(CaptureActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;

            }
        }

    };

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    // 获取手机中的图片路径
                    String[] pojo = {MediaStore.Images.Media.DATA};
                    Uri uri = data.getData();
                    CursorLoader cursorLoader = new CursorLoader(this, uri, pojo, null, null, null);
                    Cursor cursor = cursorLoader.loadInBackground();
                    // 个别手机中 cursor会为空
                    if (cursor == null) {
                        return;
                    }
                    if (cursor.moveToFirst()) {
                        photo_path = cursor.getString(cursor.getColumnIndex(pojo[0]));
                        System.out.println("pojo:" + pojo);
                        System.out.println("column:" + cursor.getColumnIndex(pojo[0]));
                        System.out.println("photo_path:" + photo_path);
                    }

                    cursor.close();
                    mProgress = new ProgressDialog(CaptureActivity.this);
                    mProgress.setMessage(getString(R.string.dingdone_string_195));
                    mProgress.setCancelable(false);
                    mProgress.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            result = scanningImage(photo_path);
                            if (result != null) {
                                Message m = mHandler.obtainMessage();
                                m.what = PARSE_BARCODE_SUC;
                                m.obj = result.getText();
                                mHandler.sendMessage(m);
                            } else {
                                Looper.prepare();
                                Message m = mHandler.obtainMessage();
                                m.what = PARSE_BARCODE_FAIL;
                                m.obj = getString(R.string.dingdone_string_196);
                                mHandler.sendMessage(m);
                                Looper.loop();
                            }
                        }
                    }).start();

                    break;

            }
        }
    }

    /**
     * 扫描二维码图片的方法
     *
     * @param path
     * @return
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);

        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_layout) {
            CaptureActivity.this.finish();
        } else if (id == R.id.button_openAlbum) {
            // 打开手机相册
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            this.startActivityForResult(intent, REQUEST_CODE);
        }
    }

}