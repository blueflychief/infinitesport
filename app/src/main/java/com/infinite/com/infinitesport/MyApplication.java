package com.infinite.com.infinitesport;

import android.app.Application;

/**
 * Created by Lsq on 6/21/2016.--9:36 AM
 */
public class MyApplication extends Application {
    public static MyApplication INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    public static MyApplication getInstance() {
        return INSTANCE;
    }
}
