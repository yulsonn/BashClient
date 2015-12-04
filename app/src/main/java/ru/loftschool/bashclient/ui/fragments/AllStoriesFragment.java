package ru.loftschool.bashclient.ui.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.adapters.AllStoriesAdapter;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.service.RefreshDataService_;
import ru.loftschool.bashclient.ui.BundleConstants;
import ru.loftschool.bashclient.ui.ToolbarInitialization;
import ru.loftschool.bashclient.ui.activities.MainActivity;
import ru.loftschool.bashclient.ui.listeners.ClickListener;

@EFragment(R.layout.fragment_all_stories)
public class AllStoriesFragment extends Fragment{

    private static final String SAVED_LAYOUT_MANAGER = "save_layout_state";
    private static AllStoriesAdapter adapter;
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private Bundle savedSelectedItems;

    @ViewById(R.id.all_stories_container)
    RecyclerView recyclerView;

    @ViewById(R.id.main_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @StringRes(R.string.frag_all_title)
    String title;

    @StringRes(R.string.undo_snackbar_story_removed)
    String storyRemovedTxt;

    @StringRes(R.string.undo_snackbar_stories_removed)
    String storiesRemovedTxt;

    @StringRes(R.string.undo_snackbar_cancel)
    String cancelTxt;

    Parcelable layoutManagerSavedState;

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public static AllStoriesAdapter getAdapter() {
        return adapter;
    }

    public ActionModeCallback getActionModeCallback() {
        return actionModeCallback;
    }

    @AfterViews
    void ready() {
        ToolbarInitialization.initToolbar(ToolbarInitialization.TOOLBAR_MAIN, (AppCompatActivity) getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
        initRecycleView();
        initSwipeRefresh();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedSelectedItems = savedInstanceState;
            layoutManagerSavedState = savedInstanceState.getParcelable(SAVED_LAYOUT_MANAGER);
        }

        loadData();
        restoreLayoutManagerPosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        initSwipeToDismiss();
    }

    private void loadData() {
        getLoaderManager().restartLoader(1, null, new LoaderManager.LoaderCallbacks<List<Story>>() {
            @Override
            public Loader<List<Story>> onCreateLoader(int id, Bundle args) {
                final AsyncTaskLoader<List<Story>> loader = new AsyncTaskLoader<List<Story>>(getActivity()) {
                    @Override
                    public List<Story> loadInBackground() {
                        return Story.selectAll();
                    }
                };
                loader.forceLoad();

                return loader;
            }

            @Override
            public void onLoadFinished(Loader<List<Story>> loader, List<Story> data) {
                SparseBooleanArray savedCurrentSelectedItems = null;
                if (adapter != null) {
                    savedCurrentSelectedItems = adapter.getSparseBooleanSelectedItems();
                }
                adapter = new AllStoriesAdapter(data, new ClickListener() {
                    @Override
                    public void onItemClicked(View view, int position) {
                        if (MainActivity.getActionMode() != null) {
                            toggleSelection(position);
                        } else {
                            switch (view.getId()) {
                                case R.id.story_favorite_flag:
                                    adapter.invertFavoriteStatus(position);
                                    break;
                                case R.id.card_view_all_stories:
                                    openFullStory(position);
                                    break;
                            }
                        }
                    }

                    @Override
                    public boolean onItemLongClicked(int position) {
                        if (MainActivity.getActionMode() == null) {
                            AppCompatActivity activity = (AppCompatActivity) getActivity();
                            MainActivity.setActionMode(activity.startSupportActionMode(actionModeCallback));
                        }
                        toggleSelection(position);
                        return true;
                    }
                });

                if (savedCurrentSelectedItems != null) {
                    adapter.setSelectedItems(savedCurrentSelectedItems);
                }

                if (savedSelectedItems != null) {
                    adapter.onRestoreInstanceState(savedSelectedItems);
                    savedSelectedItems = null;
                }
                if (adapter.getSelectedItemsCount() > 0) {
                    if (MainActivity.getActionMode() == null) {
                        AppCompatActivity activity = (AppCompatActivity) getActivity();
                        MainActivity.setActionMode(activity.startSupportActionMode(actionModeCallback));
                    }
                    MainActivity.getActionMode().setTitle(String.valueOf(adapter.getSelectedItemsCount()));
                } else if (adapter.getSelectedItemsCount() == 0 && MainActivity.getActionMode() != null) {
                    MainActivity.getActionMode().finish();
                }
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<List<Story>> loader) {

            }
        });
    }

    private void toggleSelection(int position){
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemsCount();
        if (count == 0) {
            MainActivity.getActionMode().finish();
        } else {
            MainActivity.getActionMode().setTitle(String.valueOf(count));
            MainActivity.getActionMode().invalidate();
        }
    }

    private void undoSnackbarShow() {
        Snackbar snackbar = Snackbar.make(recyclerView, adapter.getSelectedItemsCount() <= 1 ? storyRemovedTxt : storiesRemovedTxt, Snackbar.LENGTH_LONG)
                .setAction(cancelTxt, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.restoreRemovedItems();
                    }
                })
                .setActionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        snackbar.show();
        adapter.startUndoTimer(3500);
    }

    private void openFullStory(int position) {
        long id = adapter.getItemId(position);
        FullStoryFragment_ fullStoryFragment = new FullStoryFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong(BundleConstants.ARG_ID, id);
        fullStoryFragment.setArguments(bundle);

        FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fullStoryFragment).addToBackStack(null).commit();
    }

    /* INIT METHODS */

    private void initSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.yellow, R.color.primaryDark, R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RefreshDataService_.intent(getContext()).start();
            }
        });
    }

    private void initSwipeToDismiss() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                MainActivity.destroyActionModeIfNeeded();
                adapter.removeItem(viewHolder.getAdapterPosition());
                undoSnackbarShow();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void initRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new AllStoriesAdapter());
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /* ActionModeCallback */

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.cab, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.menu_remove:
                    adapter.removeItems(adapter.getSelectedItems());
                    undoSnackbarShow();
                    mode.finish();
                    return true;
                case R.id.menu_select_all:
                    adapter.selectAll(adapter.getItemCount());
                    MainActivity.getActionMode().setTitle(String.valueOf(adapter.getSelectedItemsCount()));
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            MainActivity.setActionMode(null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            adapter.onSaveInstanceState(outState);
        }
        outState.putParcelable(SAVED_LAYOUT_MANAGER, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (MainActivity.actionMode != null) {
            MainActivity.destroyActionModeIfNeeded();
        }
    }

    private void restoreLayoutManagerPosition() {
        if (layoutManagerSavedState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
        }
    }
}
