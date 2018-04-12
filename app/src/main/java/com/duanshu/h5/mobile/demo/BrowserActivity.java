package com.duanshu.h5.mobile.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import com.duanshu.h5.mobile.view.DDWebView;

/**
 * Created by conanaiflj on 2018/4/3.
 */

public class BrowserActivity extends Activity {
    private DDWebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new DDWebView(this);
        webView.loadUrl(getIntent().getStringExtra("url"));
        setContentView(webView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) ) {
            if (webView.canGoBack())
            {
                webView.goBack(); //goBack()表示返回WebView的上一页面
                return true;
            }else
            {
                finish();
                return true;
            }

        }
        return false;
    }
}
