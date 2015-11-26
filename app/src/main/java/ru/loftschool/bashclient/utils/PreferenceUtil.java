package ru.loftschool.bashclient.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.loftschool.bashclient.R;

public class PreferenceUtil {

    public static int getSyncPeriod(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int sharedSyncInterval = Integer.parseInt(pref.getString(context.getString(R.string.pref_synchronization_interval_key),
                context.getString(R.string.pref_synchronization_interval_value_default)));
        return sharedSyncInterval * 60 * 60;
    }

    public static int getNewSyncPeriod(int newValue) {
        return newValue * 60 * 60;
    }
}
