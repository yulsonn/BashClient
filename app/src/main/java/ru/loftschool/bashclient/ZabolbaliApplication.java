package ru.loftschool.bashclient;

import com.activeandroid.ActiveAndroid;

public class ZabolbaliApplication extends com.activeandroid.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }
}
