package ru.loftschool.bashclient.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ru.loftschool.bashclient.R;


public class ToolbarInitialization {
    public static final int TOOLBAR_MAIN = 0;
    public static final int TOOLBAR_ALT = 1;

    public static void initToolbar(int version, final AppCompatActivity activity){

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();

        switch (version) {
            case TOOLBAR_MAIN:
                if (actionBar != null) {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
                break;
            case TOOLBAR_ALT:
                if (actionBar != null) {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_reply_white_24dp);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
                break;
        }
    }
}