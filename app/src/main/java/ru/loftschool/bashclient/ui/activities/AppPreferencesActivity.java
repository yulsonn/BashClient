package ru.loftschool.bashclient.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.sync.AppSyncAdapter;
import ru.loftschool.bashclient.utils.PreferenceUtil;

public class AppPreferencesActivity extends AppCompatActivity {

    private static Context context;
    private String title;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_with_actionbar);
        context = getApplicationContext();
        title = getResources().getString(R.string.act_settings_title);
        setTitle(title);
        initToolbar();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new AppPreferenceFragment()).commit();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }

    public static class AppPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private SwitchPreference syncOnOff;
        private ListPreference syncInterval;
        private SwitchPreference notificationsOnOff;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            syncOnOff = (SwitchPreference) findPreference(getString(R.string.pref_enable_synchronization_key));
            syncInterval = (ListPreference) findPreference(getString(R.string.pref_synchronization_interval_key));
            notificationsOnOff = (SwitchPreference) findPreference(getString(R.string.pref_enable_notifications_key));

            syncInterval.setEnabled(syncOnOff.isChecked());
            notificationsOnOff.setEnabled(syncOnOff.isChecked());

            syncOnOff.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    syncInterval.setEnabled(syncOnOff.isChecked());
                    notificationsOnOff.setEnabled(syncOnOff.isChecked());
                    return true;
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_enable_synchronization_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_synchronization_interval_key)));

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(this);
            // Trigger the listener immediately with the preference's current value.
            if (preference.getKey().equals(context.getString(R.string.pref_enable_synchronization_key))) {
                onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getKey(), false));
            } else {
                onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }

                if (preference.getKey().equals(context.getString(R.string.pref_synchronization_interval_key))) {
                    int newSyncPeriod = PreferenceUtil.getNewSyncPeriod(Integer.parseInt(stringValue));
                    AppSyncAdapter.configurePeriodicSync(context, newSyncPeriod, newSyncPeriod / 3);
                }

            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);

                //for turn on/off sync preference
                if (preference.getKey().equals(context.getString(R.string.pref_enable_synchronization_key))) {
                    boolean value = (boolean) newValue;
                    if (value) {
                        AppSyncAdapter.syncOn(context);
                    } else {
                        AppSyncAdapter.syncOff(context);
                    }
                }
            }

            return true;
        }
    }

}
