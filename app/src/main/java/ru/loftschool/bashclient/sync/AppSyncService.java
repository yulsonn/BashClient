package ru.loftschool.bashclient.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class AppSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static AppSyncAdapter sAppSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sAppSyncAdapter == null) {
                sAppSyncAdapter = new AppSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sAppSyncAdapter.getSyncAdapterBinder();
    }
}
