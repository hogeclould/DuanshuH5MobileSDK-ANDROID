package com.duanshu.h5.mobile.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.duanshu.h5.mobile.DDPageCallBackManager;
import com.duanshu.h5.mobile.DuanshuSdk;
import com.duanshu.h5.mobile.bean.DDJsResultBean;
import com.duanshu.h5.mobile.interfaces.DuanshuAPIInterface;
import com.duanshu.h5.mobile.utils.DDJsonUtils;
import com.duanshu.h5.mobile.view.DDWebView;
import com.zhihu.matisse.Matisse;

import java.util.List;

import static com.duanshu.h5.mobile.constant.DDConstant.CODE_OK;
import static com.duanshu.h5.mobile.constant.DDConstant.REQUEST_CODE_CHOOSE;

public class MainActivity extends AppCompatActivity {
    private DDWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new DDWebView(this,null);
        DuanshuAPIInterface duanshuAPIInterface = new DuanshuAPIInterfaceImp(webView);
        DuanshuSdk.setDDAPIInterface(duanshuAPIInterface);
        DuanshuSdk.setDebug(true);
        webView.loadUrl("file:///android_asset/JS_Sdk_files/JS_Sdk.htm");
        setContentView(webView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> paths = Matisse.obtainPathResult(data);
            String json = DDJsonUtils.toJson(paths);
            DDJsResultBean res = new DDJsResultBean(CODE_OK, json);
            DDPageCallBackManager.getInstance().callBack(REQUEST_CODE_CHOOSE, DDJsonUtils.toJson(res));
        }
    }
}
