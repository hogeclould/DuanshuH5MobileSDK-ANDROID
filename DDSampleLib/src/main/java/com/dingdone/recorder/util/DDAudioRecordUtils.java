package com.dingdone.recorder.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.dingdone.recorder.LameMp3Manager;

import java.io.File;

/**
 * Created by chenwenchao on 2017/8/9.
 */

public class DDAudioRecordUtils {

    public static final String TAG = "DDAudioRecordUtils";

    private final int VOLUME_MSG_100 = 100;
    private final int TIME_MSG_101 = 101;
    private final int CANCEL_RECORD_MSG_102 = 102;
    private File audioFile = null;
    private OnFinishedRecordListener finishedListener;

    /**
     * 最短录音时间
     */
    private int MIN_INTERVAL_TIME = 1000;
    /**
     * 最长录音时间
     */
    private int MAX_INTERVAL_TIME = 1000 * 120;//120秒

    private long mStartTime;
    //private Dialog mDialog;
    //private ImageView mImageView;
    //private TextView mTitleTv, mTimeTv;
    private ObtainDecibelThread mThread;
    private Handler mVolumeHandler;
    private Context mContext;

    private static class DDAudioRecordUtilsHolder {
        static final DDAudioRecordUtils INSTANCE = new DDAudioRecordUtils();
    }

    public static DDAudioRecordUtils getInstance() {
        return DDAudioRecordUtilsHolder.INSTANCE;
    }

    private DDAudioRecordUtils() {

    }

    private void init(Context context) {
        audioFile = DDStorageUtils.getAudioRecordFile(true, System.currentTimeMillis() + ".mp3");
        if (audioFile == null) {
            if (finishedListener != null) {
                finishedListener.onRecordFail("创建文件失败");
            }
            return;
        }
        mVolumeHandler = new ShowVolumeHandler();
        mStartTime = System.currentTimeMillis();

        //录音提示的dialog, 显示录音时长, 声音大小振幅
        /*if (mDialog == null) {
            mDialog = new Dialog(context, R.style.record_alert_dialog);
            View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_record_alert, null);
            mImageView = (ImageView) contentView.findViewById(R.id.record_dialog_imageview);
            mTimeTv = (TextView) contentView.findViewById(R.id.record_dialog_time_tv);
            mTitleTv = (TextView) contentView.findViewById(R.id.record_dialog_title_tv);
            mDialog.setContentView(contentView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finishRecord();
                }
            });
        }*/
        //mDialog.show();
    }

    /****
     * 设置最大时间。
     *
     * @param time 单位秒
     */
    public void setMaxIntervalTime(int time) {
        if (time > 1) {
            MAX_INTERVAL_TIME = time * 1000;
        }
    }

    public void startRecording(Context context) {
        mContext = context;
        init(mContext);
        LameMp3Manager.getInstance().startRecorder(audioFile);
        mThread = new ObtainDecibelThread();
        mThread.start();
    }

    public void stopRecording() {
        finishRecord();
    }

    private void finishRecord() {
        stop();
        long intervalTime = System.currentTimeMillis() - mStartTime;
        if (intervalTime < MIN_INTERVAL_TIME) {
//            DDToast.showToast("时间太短");
            LameMp3Manager.getInstance().cancelRecorder();
            if (finishedListener != null)
                finishedListener.onRecordFail("时间太短");
            return;
        }
        LameMp3Manager.getInstance().stopRecorder();
    }

    public void cancelRecord() {
        LameMp3Manager.getInstance().cancelRecorder();
    }

    private void stop() {
//        if (mDialog != null) {
//            mDialog.dismiss();
//        }
        if (mThread != null) {
            mThread.exit();
            mThread = null;
        }
    }

    public void setFinishedListener(OnFinishedRecordListener listener){
        finishedListener = listener;
        LameMp3Manager.getInstance().setMediaRecorderListener(new LameMp3Manager.MediaRecorderListener() {
            @Override
            public void onRecorderFinish(String path) {
                if (finishedListener != null) {
                    finishedListener.onFinishedRecord(audioFile);
                }
            }
        });
    }

    private class ObtainDecibelThread extends Thread {

        private volatile boolean running = true;

        public void exit() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!running) {
                    break;
                }
                if (System.currentTimeMillis() - mStartTime >= MAX_INTERVAL_TIME) {
                    // 如果超过最长录音时间
                    mVolumeHandler.sendEmptyMessage(CANCEL_RECORD_MSG_102);
                }
                /*
                //dailog录音时间
                mVolumeHandler.sendEmptyMessage(TIME_MSG_101);
                //dialog剩余振幅
                int x = LameMp3Manager.getInstance().getVolume();
                if (x != 0) {
                    int f = (int) (20 * Math.log(x) / Math.log(10));
                    Message msg = new Message();
                    msg.obj = f;
                    msg.what = VOLUME_MSG_100;
                    mVolumeHandler.sendMessage(msg);
                }*/
            }
        }
    }

    class ShowVolumeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /*case VOLUME_MSG_100:
                    int tempVolumeMax = (int) msg.obj;
                    setLevel(tempVolumeMax);
                    break;
                case TIME_MSG_101:
                    long nowTime = System.currentTimeMillis();
                    int time = ((int) (nowTime - mStartTime) / 1000);
                    int second = time % 60;
                    int mil = time / 60;
                    if (mil < 10) {
                        if (second < 10)
                            mTimeTv.setText("0" + mil + ":0" + second);
                        else
                            mTimeTv.setText("0" + mil + ":" + second);
                    } else if (mil >= 10 && mil < 60) {
                        if (second < 10)
                            mTimeTv.setText(mil + ":0" + second);
                        else
                            mTimeTv.setText(mil + ":" + second);
                    }
                    break;*/
                case CANCEL_RECORD_MSG_102:
                    finishRecord();
                    break;
            }
        }
    }

    /*private void setLevel(int level) {
        if (mImageView != null)
            mImageView.getDrawable().setLevel(3000 + 6000 * level / 100);
    }*/

    /**
     * 完成录音回调
     */
    public interface OnFinishedRecordListener {
        void onFinishedRecord(File audioPath);
        void onRecordFail(String msg);
    }
}
