package ru.loftschool.bashclient;

import android.content.Context;

import com.activeandroid.ActiveAndroid;

public class ZabolbaliApplication extends com.activeandroid.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }

    public static Context getAppContext() {
        return ZabolbaliApplication.getAppContext();
    }
}
