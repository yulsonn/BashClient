package ru.loftschool.bashclient.ui.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

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

    public static final String BC_ACTION = "refresh_fragments";

    @ViewById(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @ViewById(R.id.fragment_container)
    View container;

    @ViewById(R.id.navigation_view)
    NavigationView navigationView;

    @StringRes(R.string.error_no_connection)
    String errorNoConnect;

    private FragmentTransaction transaction;

    @OptionsItem(android.R.id.home)
    void drawerOpen(){
        Fragment currentFragment = this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof FullStoryFragment) {
            super.onBackPressed();
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @OptionsItem(R.id.db_update)
    void refresh() {
        RefreshDataService_.intent(getApplication()).start();
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

    @Receiver(actions = BC_ACTION)
    protected void completeRefresh() {
        refreshFragment();
    }

    @UiThread
    void refreshFragment() {
        //TODO find how to update fragment data without recreating fragment
        Fragment currentFragment = this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof FavoriteStoriesFragment) {
            transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FavoriteStoriesFragment_());
        } else {
            transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AllStoriesFragment_());
        }

        if(!isChangingConfigurations()) {
            transaction.commit();
            transaction = null;
        }
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
                selectDrawerItem(menuItem);
                return true;
            }
        });
    }

    private void selectDrawerItem(MenuItem menuItem){
        Fragment fragment;

        switch (menuItem.getItemId()){
            case R.id.drawer_item_all:
                fragment = new AllStoriesFragment_();
                break;
            case R.id.drawer_item_favorite:
                fragment = new FavoriteStoriesFragment_();
                break;
            default:
                fragment = new AllStoriesFragment_();
        }

        replaceFragment(fragment);
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
    }

    private void replaceFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();
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
            finish();
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
}