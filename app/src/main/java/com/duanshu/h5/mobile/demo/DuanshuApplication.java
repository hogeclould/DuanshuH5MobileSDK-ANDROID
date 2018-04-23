package com.duanshu.h5.mobile.demo;

import android.app.Application;

import com.duanshu.h5.mobile.DuanshuSdk;

/**
 * Created by conanaiflj on 2018/4/17.
 */

public class DuanshuApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        DuanshuSdk.init(this);
    }
}
