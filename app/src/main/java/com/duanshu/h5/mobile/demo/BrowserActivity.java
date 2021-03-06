package com.duanshu.h5.mobile.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import com.duanshu.h5.mobile.DDPageCallBackManager;
import com.duanshu.h5.mobile.bean.DDJsResultBean;
import com.duanshu.h5.mobile.demo.bean.DDFileBean;
import com.duanshu.h5.mobile.demo.callback.MultiFileBytesCallback;
import com.duanshu.h5.mobile.utils.DDJsonUtils;
import com.duanshu.h5.mobile.view.DDWebView;
import com.zhihu.matisse.Matisse;

import java.util.List;

import static com.duanshu.h5.mobile.constant.DDConstant.CODE_FAIL;
import static com.duanshu.h5.mobile.constant.DDConstant.CODE_OK;
import static com.duanshu.h5.mobile.constant.DDConstant.REQUEST_CODE_CHOOSE;
import static com.duanshu.h5.mobile.constant.DDConstant.REQUEST_CODE_CHOOSE_WITHBYTE;

/**
 * Created by conanaiflj on 2018/4/3.
 */

public class BrowserActivity extends BaseActivity {

    private DDWebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new DDWebView(this);
        webView.setDuanshuSdkImpl(new DuanshuAPIInterfaceImp(webView,null));
        webView.loadUrl(getIntent().getStringExtra("url"));
        setContentView(webView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (webView.canGoBack()) {
                webView.goBack(); //goBack()表示返回WebView的上一页面
                return true;
            } else {
                finish();
                return true;
            }

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> paths = Matisse.obtainPathResult(data);
            if (paths != null && paths.size() > 0) {
                DDJsResultBean res = new DDJsResultBean(CODE_OK, "照片选取成功");
                res.data = paths;
                DDPageCallBackManager.getInstance().callBack(REQUEST_CODE_CHOOSE, DDJsonUtils.toJson(res));
            } else {
                DDJsResultBean res = new DDJsResultBean(CODE_FAIL, "照片选取失败");
                DDPageCallBackManager.getInstance().callBack(REQUEST_CODE_CHOOSE, DDJsonUtils.toJson(res));
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE_WITHBYTE && resultCode == RESULT_OK) {
            List<String> paths = Matisse.obtainPathResult(data);
            if (paths != null && paths.size() > 0) {
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
            } else {
                DDJsResultBean res = new DDJsResultBean(CODE_FAIL, "照片选取失败");
                DDPageCallBackManager.getInstance().callBack(REQUEST_CODE_CHOOSE_WITHBYTE, DDJsonUtils.toJson(res));
            }


        }
    }
}
