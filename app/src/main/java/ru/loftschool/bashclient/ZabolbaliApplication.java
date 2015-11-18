package ru.loftschool.bashclient;

import com.activeandroid.ActiveAndroid;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class ZabolbaliApplication extends com.activeandroid.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        ActiveAndroid.initialize(this);
    }
}
