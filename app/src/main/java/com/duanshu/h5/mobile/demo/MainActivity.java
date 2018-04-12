package com.duanshu.h5.mobile.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ddapp.my.ddqrcode.activity.CaptureActivity;
import com.dingdone.recorder.util.SPUtils;
import com.duanshu.h5.mobile.DDPageCallBackManager;
import com.duanshu.h5.mobile.DuanshuSdk;
import com.duanshu.h5.mobile.bean.DDJsResultBean;
import com.duanshu.h5.mobile.constant.DDConstant;
import com.duanshu.h5.mobile.demo.bean.DDFileBean;
import com.duanshu.h5.mobile.demo.bean.DDShareBean;
import com.duanshu.h5.mobile.demo.callback.IViewUpdate;
import com.duanshu.h5.mobile.demo.callback.MultiFileBytesCallback;
import com.duanshu.h5.mobile.interfaces.DuanshuAPIInterface;
import com.duanshu.h5.mobile.utils.DDJsonUtils;
import com.duanshu.h5.mobile.view.DDWebView;
import com.zhihu.matisse.Matisse;

import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

import static com.duanshu.h5.mobile.constant.DDConstant.CODE_FAIL;
import static com.duanshu.h5.mobile.constant.DDConstant.CODE_OK;
import static com.duanshu.h5.mobile.constant.DDConstant.REQUEST_CODE_CHOOSE;
import static com.duanshu.h5.mobile.constant.DDConstant.REQUEST_CODE_CHOOSE_WITHBYTE;

public class MainActivity extends AppCompatActivity implements IViewUpdate{
    private DDWebView webView;
    private boolean isShowShare = false;//是否显示share menu
    private DuanshuAPIInterface duanshuAPIInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.qrcode:
                        Intent it = new Intent(MainActivity.this, CaptureActivity.class);
                        startActivity(it);
                        break;
                    case R.id.share:
                        DDShareBean shareBean = (DDShareBean) SPUtils.getObject(MainActivity.this, webView.getUrl());
                        if(shareBean != null){
                            //弹出分享
                            OnekeyShare onekeyShare = new OnekeyShare();
                            onekeyShare.setTitle(shareBean.title);
                            onekeyShare.setImageUrl(shareBean.picurl);
                            onekeyShare.setText(shareBean.content);
                            onekeyShare.setUrl(shareBean.url);
                            onekeyShare.setCallback(new PlatformActionListener() {
                                @Override
                                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                                    DDJsResultBean bean = new DDJsResultBean();
                                    bean.code = DDConstant.CODE_OK;
                                    bean.msg = "分享成功";
                                }

                                @Override
                                public void onError(Platform platform, int i, Throwable throwable) {
                                    DDJsResultBean bean = new DDJsResultBean();
                                    bean.code = DDConstant.CODE_FAIL;
                                    bean.msg = "分享失败";
                                }

                                @Override
                                public void onCancel(Platform platform, int i) {

                                }
                            });
                            onekeyShare.show(MainActivity.this);
                        }
                        break;
                }
                return true;
            }
        });

        webView = findViewById(R.id.webview);
        duanshuAPIInterface = new DuanshuAPIInterfaceImp(webView, this);
        DuanshuSdk.setDDAPIInterface(duanshuAPIInterface);
        DuanshuSdk.setDebug(true);
        webView.loadUrl("http://file.dingdone.com/dddoc/jssdk/Duanshu-h5sdk-API-Demo.html");
//        webView.loadUrl("file:///android_asset/JS_Sdk_files/JS_Sdk.htm");
//        setContentView(webView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> paths = Matisse.obtainPathResult(data);
            if(paths!=null && paths.size() > 0){
                DDJsResultBean res = new DDJsResultBean(CODE_OK, "照片选取成功");
                res.data = paths;
                DDPageCallBackManager.getInstance().callBack(REQUEST_CODE_CHOOSE, DDJsonUtils.toJson(res));
            }else{
                DDJsResultBean res = new DDJsResultBean(CODE_FAIL, "照片选取失败");
                DDPageCallBackManager.getInstance().callBack(REQUEST_CODE_CHOOSE, DDJsonUtils.toJson(res));
            }
        }else if(requestCode == REQUEST_CODE_CHOOSE_WITHBYTE && resultCode == RESULT_OK){
            List<String> paths = Matisse.obtainPathResult(data);
            if(paths!=null && paths.size() > 0){
                FilesTask task = new FilesTask(paths);
                task.setMultiFileBytesCallback(new MultiFileBytesCallback() {
                    @Override
                    public void fileBytes(List<DDFileBean> base64s) {
                        DDJsResultBean res = new DDJsResultBean(CODE_OK, "照片选取成功");
                        res.data = base64s;
                        DDPageCallBackManager.getInstance().callBack(REQUEST_CODE_CHOOSE_WITHBYTE, DDJsonUtils.toJson(res));
                    }
                });
                task.executeMuti();
            }else{
                DDJsResultBean res = new DDJsResultBean(CODE_FAIL, "照片选取失败");
                DDPageCallBackManager.getInstance().callBack(REQUEST_CODE_CHOOSE_WITHBYTE, DDJsonUtils.toJson(res));
            }


        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.share).setVisible(isShowShare);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void updateShareMenu(boolean isVisible) {
        isShowShare = isVisible;
        invalidateOptionsMenu();
    }
}
