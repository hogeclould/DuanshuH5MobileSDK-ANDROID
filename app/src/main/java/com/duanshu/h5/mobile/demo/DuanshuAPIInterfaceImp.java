package com.duanshu.h5.mobile.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.dingdone.recorder.util.DDAudioRecordUtils;
import com.dingdone.recorder.util.DDMediaPlayerUtils;
import com.duanshu.h5.mobile.DDPageCallBackManager;
import com.duanshu.h5.mobile.bean.DDJsResultBean;
import com.duanshu.h5.mobile.callback.CallBackFunction;
import com.duanshu.h5.mobile.constant.DDConstant;
import com.duanshu.h5.mobile.demo.bean.UserInfo;
import com.duanshu.h5.mobile.interfaces.DuanshuAPIInterface;
import com.duanshu.h5.mobile.utils.DDJsonUtils;
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
    private View webView;

    public DuanshuAPIInterfaceImp(View webView) {
        this.webView = webView;
        this.context = webView.getContext();
    }

    @Override
    public void getUserInfo(CallBackFunction callBackFunction) {
        DDJsResultBean bean = new DDJsResultBean();
        bean.code = DDConstant.CODE_OK;
        bean.msg = "成功";
        UserInfo userInfo = new UserInfo();
        userInfo.userName = "用户名";
        userInfo.userId = "用户id";
        userInfo.avatarUrl = "用户头像链接";
        userInfo.telephone = "绑定手机号";
        bean.data = userInfo;
        callBackFunction.onCallBack(DDJsonUtils.toJson(bean));
    }

    @Override
    public void previewPic(Map<String,Object> data) {
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
                                    DDPageCallBackManager.getInstance().addMap(DDConstant.REQUEST_CODE_CHOOSE,callBackFunction);
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
                                            .forResult(DDConstant.REQUEST_CODE_CHOOSE);


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
        String title = data.get("title") + "";
        String content = data.get("content") + "";
        String picurl = data.get("picurl") + "";
        String url = data.get("url") + "";

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

    @Override
    public void previewImage(Map<String,Object> data) {
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
    public void startRecord(final CallBackFunction callBack) {
        DDAudioRecordUtils.getInstance().startRecording(context);
        DDAudioRecordUtils.getInstance().setFinishedListener(new DDAudioRecordUtils.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(File audioPath) {
                DDJsResultBean bean = new DDJsResultBean();
                bean.code = DDConstant.CODE_OK;
                bean.msg = "记录完成";
                bean.data = audioPath.getAbsolutePath();
                callBack.onCallBack(DDJsonUtils.toJson(bean));
            }

            @Override
            public void onRecordFail(String msg) {
//                DDAudioRecordUtils.getInstance().startRecording(context);
                DDJsResultBean bean = new DDJsResultBean();
                bean.code = DDConstant.CODE_METHOD_ERROR;
                bean.msg = msg;
                callBack.onCallBack(DDJsonUtils.toJson(bean));
            }
        });
    }

    @Override
    public void stopRecord(final CallBackFunction callBack) {
        DDAudioRecordUtils.getInstance().setFinishedListener(new DDAudioRecordUtils.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(File audioPath) {
                DDJsResultBean bean = new DDJsResultBean();
                bean.code = DDConstant.CODE_OK;
                bean.msg = "记录完成";
                bean.data = audioPath.getAbsolutePath();
                callBack.onCallBack(DDJsonUtils.toJson(bean));
            }

            @Override
            public void onRecordFail(String msg) {
//                DDAudioRecordUtils.getInstance().startRecording(context);
                DDJsResultBean bean = new DDJsResultBean();
                bean.code = DDConstant.CODE_METHOD_ERROR;
                bean.msg = msg;
                callBack.onCallBack(DDJsonUtils.toJson(bean));
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
    public void pauseVoice() {
        DDMediaPlayerUtils.pauseVoice();
    }

    @Override
    public void stopVoice() {
        DDMediaPlayerUtils.stop();
    }

}
