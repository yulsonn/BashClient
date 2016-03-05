package ru.loftschool.bashclient.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import retrofit.RetrofitError;
import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.utils.NotificationUtil;
import ru.loftschool.bashclient.utils.PreferenceUtil;
import ru.loftschool.bashclient.utils.UpdateDataUtil;

public class AppSyncAdapter extends AbstractThreadedSyncAdapter{

    private static final String TAG = AppSyncAdapter.class.getSimpleName();

    public AppSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
        Log.i(TAG, "initializeSyncAdapter");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            UpdateDataUtil.loadData();
            NotificationUtil.UpdateNotifications(getContext());
        } catch (RetrofitError e) {
            Log.e(TAG, "Retrofit Error. Data loading failed.");
        }
    }

    public static void syncImmediately (Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    // check if account already created on the device, if no - it will be created -> onAccountCreated()
    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if ( null == accountManager.getPassword(newAccount) ) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }

        return newAccount;
    }


    // this method response for creating an account
    private static void onAccountCreated(Account newAccount, Context context) {
        final int SYNC_INTERVAL = PreferenceUtil.getSyncPeriod(context);
        final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

        AppSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        ContentResolver.addPeriodicSync(newAccount, context.getString(R.string.content_authority), Bundle.EMPTY, SYNC_INTERVAL);
        syncImmediately(context);
    }

    // configure periodical synchronization
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    //stop synchronization
    public static void syncOff(Context context) {
        ContentResolver.setIsSyncable(getSyncAccount(context), context.getString(R.string.content_authority), 0);
        Log.i(TAG, "syncOff");
    }

    //start synchronization
    public static void syncOn(Context context) {
        ContentResolver.setIsSyncable(getSyncAccount(context), context.getString(R.string.content_authority), 1);
        Log.i(TAG, "syncOn");
    }

}
