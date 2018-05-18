package com.duanshu.h5.mobile.demo;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Jason on 2018/5/18.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DuanshuApplication.getInstance().setWatchTarget(this);
    }
}
