package ru.loftschool.bashclient.ui.listeners;

import android.view.View;

public interface ClickListener {
    void onItemClicked(View view, int position);
    boolean onItemLongClicked(int position);
}
