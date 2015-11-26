package ru.loftschool.bashclient.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class AppAuthenticatorService extends Service {

    private AppAuthenticator mAppAuthenticator;

    @Override
    public void onCreate() {
        mAppAuthenticator = new AppAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAppAuthenticator.getIBinder();
    }
}
