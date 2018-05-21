package com.duanshu.h5.mobile.demo;

import android.app.Application;

import com.duanshu.h5.mobile.DuanshuSdk;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by conanaiflj on 2018/4/17.
 */

public class DuanshuApplication extends Application {



    private static DuanshuApplication INSTANCE;
    private RefWatcher mRefWatcher;

    public static DuanshuApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        tryInstallCanary();
        DuanshuSdk.setDebug(true);
        DuanshuSdk.init(this);
    }


    public void setWatchTarget(Object o) {
        mRefWatcher.watch(o);
    }

    private void tryInstallCanary() {
//        BlockCanary.install(this, new BlockCanaryContext()).start();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        mRefWatcher = LeakCanary.install(this);
    }
}
