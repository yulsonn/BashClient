package ru.loftschool.bashclient.ui.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.service.RefreshDataService_;
import ru.loftschool.bashclient.ui.ToolbarInitialization;
import ru.loftschool.bashclient.ui.dialogs.AboutDialogFragment;
import ru.loftschool.bashclient.ui.fragments.AllStoriesFragment;
import ru.loftschool.bashclient.ui.fragments.AllStoriesFragment_;
import ru.loftschool.bashclient.ui.fragments.FavoriteStoriesFragment;
import ru.loftschool.bashclient.ui.fragments.FavoriteStoriesFragment_;
import ru.loftschool.bashclient.ui.fragments.FullStoryFragment;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.menu_main)
public class MainActivity extends AppCompatActivity {

    public static final String REFRESH_FRAGMENTS_ACTION = "refresh_fragments";
    public static final String SWIPE_START_ACTION = "start_swipe_refresh";
    public static final String SWIPE_STOP_ACTION = "stop_swipe_refresh";

    public static ActionMode actionMode;
    private SparseBooleanArray currentSelectedItems;
    private ActionBarDrawerToggle mDrawerToggle;

    private static final int TIME_INTERVAL = 2000; // 2 seconds - desired time passed between two back presses.
    private long mBackPressed;

    @ViewById(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @ViewById(R.id.fragment_container)
    View container;

    @ViewById(R.id.navigation_view)
    NavigationView navigationView;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @StringRes(R.string.error_no_connection)
    String errorNoConnect;

    @StringRes(R.string.exit_toast_text)
    String exitToastText;

    private FragmentTransaction transaction;

    public static ActionMode getActionMode() {
        return actionMode;
    }

    public static void setActionMode(ActionMode actionMode) {
        MainActivity.actionMode = actionMode;
    }

    @OptionsItem(android.R.id.home)
    void drawerOpen(){
        Fragment currentFragment = this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof FullStoryFragment) {
            super.onBackPressed();
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @OptionsItem(R.id.action_open_about)
    void info() {
        FragmentManager fm = getSupportFragmentManager();
        AboutDialogFragment dialogFragment = new AboutDialogFragment();
        dialogFragment.show(fm, null);
    }

    @AfterViews
    void ready() {
        ToolbarInitialization.initToolbar(ToolbarInitialization.TOOLBAR_MAIN, this);
        initNavigationDrawer();
        initStories();
    }

    @Receiver(actions = REFRESH_FRAGMENTS_ACTION)
    protected void completeRefresh() {
        refreshFragment();
    }

    @Receiver(actions = SWIPE_START_ACTION)
    protected void startSwipeRefresh() {
        swipeRefreshVisible(true);
    }

    @Receiver(actions = SWIPE_STOP_ACTION)
    protected void stopSwipeRefresh() {
        swipeRefreshVisible(false);
    }

    void swipeRefreshVisible(boolean isVisible) {
        SwipeRefreshLayout swipe = ((AllStoriesFragment) this.getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getSwipeRefreshLayout();
        if (swipe != null) {
            swipe.setRefreshing(isVisible);
        }
    }

    @UiThread
    void refreshFragment() {
       transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AllStoriesFragment_());

        if(!isChangingConfigurations()) {
            transaction.commit();
            transaction = null;
        }

        // TODO: quantity downloaded items for adapter update instead of Fragment reload
        //AllStoriesFragment.getAdapter().notifyItemRangeInserted(0,5);

    }

    private void initStories() {
        if (Story.selectAll().isEmpty()) {
            RefreshDataService_.intent(getApplication()).start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AllStoriesFragment_()).commit();
        }
    }

    private void initNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                destroyActionModeIfNeeded();
                selectDrawerItem(menuItem);
                return true;
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                if(newState == DrawerLayout.STATE_DRAGGING){
                    if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                        //closing drawer
                        if (currentSelectedItems != null) {
                            startActionMode();
                        }
                    } else {
                        //opening drawer
                        if (actionMode != null) {
                            saveAndStopActionMode();
                        }
                    }
                }
            }
        };

        // set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    // saving selected items and finishing Action Mode
    private void saveAndStopActionMode() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof AllStoriesFragment) {
            currentSelectedItems = AllStoriesFragment.getAdapter().getSparseBooleanSelectedItems().clone();
        } else if (currentFragment instanceof FavoriteStoriesFragment) {
            currentSelectedItems = FavoriteStoriesFragment.getAdapter().getSparseBooleanSelectedItems().clone();
        }
        actionMode.finish();
    }

    // starting Action Mode and restoring selected items
    private void startActionMode() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof AllStoriesFragment) {
            AllStoriesFragment.getAdapter().setSelectedItems(currentSelectedItems.clone());
            AllStoriesFragment.getAdapter().notifyDataSetChanged();
            actionMode = startSupportActionMode(((AllStoriesFragment) currentFragment).getActionModeCallback());
            actionMode.setTitle(String.valueOf(AllStoriesFragment.getAdapter().getSelectedItemsCount()));
        } else if (currentFragment instanceof FavoriteStoriesFragment) {
            FavoriteStoriesFragment.getAdapter().setSelectedItems(currentSelectedItems.clone());
            FavoriteStoriesFragment.getAdapter().notifyDataSetChanged();
            actionMode = startSupportActionMode(((FavoriteStoriesFragment) currentFragment).getActionModeCallback());
            actionMode.setTitle(String.valueOf(FavoriteStoriesFragment.getAdapter().getSelectedItemsCount()));
        }
        currentSelectedItems = null;
    }

    private void selectDrawerItem(MenuItem menuItem){
        Fragment fragment = null;

        switch (menuItem.getItemId()){
            case R.id.drawer_item_all:
                fragment = new AllStoriesFragment_();
                break;
            case R.id.drawer_item_favorite:
                fragment = new FavoriteStoriesFragment_();
                break;
        }

        if (fragment != null) {
            replaceFragment(fragment);
            menuItem.setChecked(true);
            drawerLayout.closeDrawers();
        }
    }

    private void replaceFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getSimpleName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPooped = fragmentManager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPooped && fragmentManager.findFragmentByTag(backStateName) == null) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, backStateName)
                                                .addToBackStack(backStateName)
                                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                                                .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (this.getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof AllStoriesFragment) {
            Toast toast = Toast.makeText(getBaseContext(), exitToastText, Toast.LENGTH_SHORT);

            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
            {
                toast.cancel();
                finish();
            }
            else {
                toast.show();
            }

            mBackPressed = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (transaction != null) {
            transaction.commit();
        }
    }

    public static void destroyActionModeIfNeeded() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }
}