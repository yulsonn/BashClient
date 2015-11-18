package ru.loftschool.bashclient.service;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringRes;

import retrofit.RetrofitError;
import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.activities.MainActivity;
import ru.loftschool.bashclient.utils.NetworkConnectionChecker;
import ru.loftschool.bashclient.utils.UpdateDataUtil;

@EIntentService
public class RefreshDataService extends IntentService {

    @StringRes(R.string.error_no_connection)
    String errorNoConnect;

    @StringRes(R.string.error_troubles_connection)
    String errorTroubleConnect;

    @StringRes(R.string.error_troubles_server)
    String errorTroubleServer;

    @StringRes(R.string.message_success_update)
    String mesSuccess;

    public RefreshDataService() {
        super(RefreshDataService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        updateStories();
    }

    public void updateStories() {
        if (NetworkConnectionChecker.isConnected(this)) {

            if (Story.selectAll().size() == 0) {
                swipeRefreshStart();
            }

            try {
                UpdateDataUtil.loadData();
            } catch (RetrofitError e) {
                retrofitErrorMessageShow(e.getKind(), e);
                swipeRefreshStop();
                return;
            }

            refreshFragments();
            showMessage(mesSuccess);
            swipeRefreshStop();
        } else {
            showMessage(errorNoConnect);
            swipeRefreshStop();
        }
    }

    @UiThread
    void retrofitErrorMessageShow(RetrofitError.Kind kind, RetrofitError error) {
        if (kind.equals(RetrofitError.Kind.NETWORK)) {
            Toast.makeText(this, errorTroubleConnect, Toast.LENGTH_SHORT).show();
        } else if (kind.equals(RetrofitError.Kind.CONVERSION) || kind.equals(RetrofitError.Kind.HTTP)) {
            Toast.makeText(this, errorTroubleServer, Toast.LENGTH_SHORT).show();
        } else {
            throw error;
        }
    }

    @UiThread
    void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    void refreshFragments() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.REFRESH_FRAGMENTS_ACTION);
        sendBroadcast(broadcastIntent);
    }

    void swipeRefreshStart() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.SWIPE_START_ACTION);
        sendBroadcast(broadcastIntent);
    }

    void swipeRefreshStop() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.SWIPE_STOP_ACTION);
        sendBroadcast(broadcastIntent);
    }
}