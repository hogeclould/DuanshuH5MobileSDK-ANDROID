package com.duanshu.h5.mobile.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.dingdone.recorder.util.DDAudioRecordUtils;
import com.dingdone.recorder.util.DDMediaPlayerUtils;
import com.dingdone.recorder.util.SPUtils;
import com.duanshu.h5.mobile.DDPageCallBackManager;
import com.duanshu.h5.mobile.bean.DDJsResultBean;
import com.duanshu.h5.mobile.callback.CallBackFunction;
import com.duanshu.h5.mobile.constant.DDConstant;
import com.duanshu.h5.mobile.demo.bean.DDFileBean;
import com.duanshu.h5.mobile.demo.bean.DDShareBean;
import com.duanshu.h5.mobile.demo.bean.UserInfo;
import com.duanshu.h5.mobile.demo.callback.IViewUpdate;
import com.duanshu.h5.mobile.demo.callback.SingleFileBytesCallback;
import com.duanshu.h5.mobile.interfaces.DuanshuAPIInterface;
import com.duanshu.h5.mobile.utils.DDJsonUtils;
import com.duanshu.h5.mobile.utils.DDUtil;
import com.duanshu.h5.mobile.view.DDWebView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by conanaiflj on 2018/3/21.
 */

public class DuanshuAPIInterfaceImp implements DuanshuAPIInterface{
    private Context context;
    private DDWebView webView;
    private IViewUpdate iViewUpdate;

    public DuanshuAPIInterfaceImp(DDWebView webView, IViewUpdate iViewUpdate) {
        this.webView = webView;
        this.context = webView.getContext();
        this.iViewUpdate = iViewUpdate;
    }

    @Override
    public void getUserInfo(Map<String, Object> data, CallBackFunction callBackFunction) {
        DDJsResultBean bean = new DDJsResultBean();
        bean.code = DDConstant.CODE_OK;
        bean.msg = "成功";
        UserInfo userInfo = new UserInfo();
        userInfo.userName = "李四";
        userInfo.userId = "k888hhggggg";
        userInfo.avatarUrl = "https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=818777786,1911403333&fm=173&app=12&f=JPEG?w=218&h=146&s=2C70208C45430FE934932F9D0300508E";
        userInfo.telephone = "18652053671";
        bean.data = userInfo;
        callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
    }

    @Override
    public void previewPic(Map<String, Object> data, CallBackFunction callBackFunction) {
        String positionStr = data.get("position") + "";
        int position = 0;
        try {
            position = Integer.parseInt(positionStr);
        }catch (Exception e){

        }

        ArrayList<String> pics = (ArrayList<String>) data.get("pics");
        if(pics!=null && pics.size()>0){
            Intent it = new Intent();
            Uri uri = Uri.parse("duanshu://com.duanshu.h5.mobile/previewpics");
            it.setData(uri);
            it.putExtra("position",position);
            it.putStringArrayListExtra("pics",pics);
            it.setAction(Intent.ACTION_VIEW);
            if (it.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(it);
            }

        }
    }

    @Override
    public void chooseImage(Map<String,Object> data, final CallBackFunction callBackFunction) {
        String countStr = data.get("count") + "";
        String base64_enabled = data.get("base64_enabled") + "";
        double double_base64_enabled = 0;
        try {
            double_base64_enabled = Double.parseDouble(base64_enabled);
        }catch (Exception e){
        }

        final boolean isReturnBase64 = (double_base64_enabled == 1);
        int count = 9;
        try {
            count = Integer.parseInt(countStr);
        }catch (Exception e){

        }
        final int finalCount = count;
        webView.post(new Runnable() {
            @Override
            public void run() {
                RxPermissions rxPermissions = new RxPermissions((Activity) context);
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                if(aBoolean){
                                    DDPageCallBackManager.getInstance().addMap(isReturnBase64?DDConstant.REQUEST_CODE_CHOOSE_WITHBYTE:DDConstant.REQUEST_CODE_CHOOSE,callBackFunction);
                                    Matisse.from((Activity) context)
                                            .choose(MimeType.ofImage(), false)
                                            .countable(true)
                                            .capture(true)
                                            .captureStrategy(
                                                    new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider"))
                                            .maxSelectable(finalCount)
                                            .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                                            .gridExpectedSize(
                                                    context.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                            .thumbnailScale(0.85f)
                                            .imageEngine(new GlideEngine())
                                            .forResult(isReturnBase64?DDConstant.REQUEST_CODE_CHOOSE_WITHBYTE:DDConstant.REQUEST_CODE_CHOOSE);


                                }else{
                                    Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG)
                                            .show();
                                    DDJsResultBean bean = new DDJsResultBean();
                                    bean.code = DDConstant.CODE_FAIL;
                                    bean.msg = "授权失败";
                                    callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                DDJsResultBean bean = new DDJsResultBean();
                                bean.code = DDConstant.CODE_FAIL;
                                bean.msg = "授权失败";
                                callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
                            }

                            @Override
                            public void onComplete() {

                            }
                        });

            }
        });

    }

    @Override
    public void share(Map<String,Object> data, final CallBackFunction callBackFunction) {
        final String title = data.get("title") + "";
        final String content = data.get("content") + "";
        final String picurl = data.get("picurl") + "";
        final String url = data.get("url") + "";
        final String showShareButton = data.get("showShareButton") + "";
        final String updateShareData = data.get("updateShareData") + "";
        iViewUpdate.updateShareMenu(TextUtils.equals("1",showShareButton));
        if(TextUtils.equals("1",updateShareData)){
            //存储数据
            final DDShareBean shareBean = new DDShareBean(title, content, picurl, url, showShareButton, updateShareData);
            webView.post(new Runnable() {
                @Override
                public void run() {
                    SPUtils.putObject(context,webView.getUrl(), shareBean);
                }
            });

        }else{
            //弹出分享
            OnekeyShare onekeyShare = new OnekeyShare();
            onekeyShare.setTitle(title);
            onekeyShare.setImageUrl(picurl);
            onekeyShare.setText(content);
            onekeyShare.setUrl(url);
            onekeyShare.setCallback(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    DDJsResultBean bean = new DDJsResultBean();
                    bean.code = DDConstant.CODE_OK;
                    bean.msg = "分享成功";
                    callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {
                    DDJsResultBean bean = new DDJsResultBean();
                    bean.code = DDConstant.CODE_FAIL;
                    bean.msg = "分享失败";
                    callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
                }

                @Override
                public void onCancel(Platform platform, int i) {

                }
            });
            onekeyShare.show(context);

        }

    }

    @Override
    public void previewImage(Map<String, Object> data, CallBackFunction callBackFunction) {
        String imgUrl = data.get("imgUrl") + "";
        Intent it = new Intent();
        Uri uri = Uri.parse("duanshu://com.duanshu.h5.mobile/previewimage");
        it.setData(uri);
        it.putExtra("imgUrl",imgUrl);
        it.setAction(Intent.ACTION_VIEW);
        if (it.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(it);
        }
    }

    @Override
    public void startRecord(Map<String, Object> data, final CallBackFunction callBackFunction) {
        String base64_enabled = "";
        if(data!=null){
            base64_enabled = data.get("base64_enabled") + "";
        }
        double double_base64_enabled = 0;
        try {
            double_base64_enabled = Double.parseDouble(base64_enabled);
        }catch (Exception e){
        }

        final boolean isReturnBase64 = (double_base64_enabled == 1);
        DDAudioRecordUtils.getInstance().setReturnBase64(isReturnBase64);
        DDAudioRecordUtils.getInstance().startRecording(context);
        DDAudioRecordUtils.getInstance().setFinishedListener(new DDAudioRecordUtils.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(File audioPath) {
                if(!isReturnBase64){
                    DDJsResultBean bean = new DDJsResultBean();
                    bean.code = DDConstant.CODE_OK;
                    bean.msg = "记录完成";
                    bean.data = audioPath.getAbsolutePath();
                    callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
                }else{
                    FilesTask task = new FilesTask(audioPath.getAbsolutePath());
                    task.setSingleFileBytesCallback(new SingleFileBytesCallback() {
                        @Override
                        public void fileBytes(DDFileBean base64) {
                            DDJsResultBean res = new DDJsResultBean(DDConstant.CODE_OK, "记录完成");
                            res.data = base64;
                            callBackFunction.onCallBack(DDJsonUtils.toJson(res));
                        }
                    });
                    task.executeSingle();
                }

            }

            @Override
            public void onRecordFail(String msg) {
//                DDAudioRecordUtils.getInstance().startRecording(context);
                DDJsResultBean bean = new DDJsResultBean();
                bean.code = DDConstant.CODE_METHOD_ERROR;
                bean.msg = msg;
                callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
            }
        });
    }

    @Override
    public void stopRecord(Map<String, Object> data, final CallBackFunction callBackFunction) {
        final boolean isReturnBase64 = DDAudioRecordUtils.getInstance().isReturnBase64();
        DDAudioRecordUtils.getInstance().setFinishedListener(new DDAudioRecordUtils.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(File audioPath) {
                if(!isReturnBase64){
                    DDJsResultBean bean = new DDJsResultBean();
                    bean.code = DDConstant.CODE_OK;
                    bean.msg = "记录完成";
                    bean.data = audioPath.getAbsolutePath();
                    callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
                }else{
                    FilesTask task = new FilesTask(audioPath.getAbsolutePath());
                    task.setSingleFileBytesCallback(new SingleFileBytesCallback() {
                        @Override
                        public void fileBytes(DDFileBean base64) {
                            DDJsResultBean res = new DDJsResultBean(DDConstant.CODE_OK, "记录完成");
                            res.data = base64;
                            callBackFunction.onCallBack(DDJsonUtils.toJson(res));
                        }
                    });
                    task.executeSingle();
                }
            }

            @Override
            public void onRecordFail(String msg) {
//                DDAudioRecordUtils.getInstance().startRecording(context);
                DDJsResultBean bean = new DDJsResultBean();
                bean.code = DDConstant.CODE_METHOD_ERROR;
                bean.msg = msg;
                callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
            }
        });
        DDAudioRecordUtils.getInstance().stopRecording();
    }

    @Override
    public void playVoice(Map<String,Object> data, final CallBackFunction callBackFunction) {
        String record_url = data.get("record_url") + "";
        DDMediaPlayerUtils.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                DDJsResultBean bean = new DDJsResultBean();
                bean.code = DDConstant.CODE_OK;
                bean.msg = "播放完成";
                callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
            }
        });
        DDMediaPlayerUtils.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                DDJsResultBean bean = new DDJsResultBean();
                bean.code = DDConstant.CODE_FAIL;
                bean.msg = "播放失败";
                callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
                return false;
            }
        });
        DDMediaPlayerUtils.play(context, record_url);
    }

    @Override
    public void pauseVoice(Map<String, Object> data, CallBackFunction callBackFunction) {
        DDMediaPlayerUtils.pauseVoice();
    }

    @Override
    public void stopVoice(Map<String, Object> data, CallBackFunction callBackFunction) {
        DDMediaPlayerUtils.stop();
    }

    @Override
    public void loadUrl(Map<String, Object> data, CallBackFunction callBack) {
        String url = data.get("url") + "";
        if (url.startsWith("dingdone")) {
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if(TextUtils.equals("dingdone",scheme) && TextUtils.equals("tel",host)){
                String tel = uri.getQueryParameter("phone_number");
                DDUtil.makeCall(webView.getContext(), tel);
            }
        }else if (url.startsWith("http") || url.startsWith("https")) {
            Intent it = new Intent();
            Uri uri = Uri.parse("duanshu://com.duanshu.h5.mobile/browser");
            it.setData(uri);
            it.putExtra("url",url);
            it.setAction(Intent.ACTION_VIEW);
            if (it.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(it);
            }
        }
    }

}
