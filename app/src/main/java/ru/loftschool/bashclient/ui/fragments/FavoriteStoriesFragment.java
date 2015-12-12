package ru.loftschool.bashclient.ui.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.adapters.FavoriteStoriesAdapter;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.BundleConstants;
import ru.loftschool.bashclient.ui.ToolbarInitialization;
import ru.loftschool.bashclient.ui.activities.MainActivity;
import ru.loftschool.bashclient.ui.listeners.ClickListener;
import ru.loftschool.bashclient.utils.RemoveSituation;

@EFragment(R.layout.fragment_favorite_stories)
public class FavoriteStoriesFragment extends Fragment implements RemoveSituation {
    private static final String SAVED_LAYOUT_MANAGER = "save_layout_state";

    private static FavoriteStoriesAdapter adapter;
    private Bundle savedSelectedItems;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();

    @ViewById(R.id.favorite_stories_container)
    RecyclerView recyclerView;

    @StringRes(R.string.frag_favorites_title)
    String title;

    @StringRes(R.string.undo_snackbar_story_removed)
    String storyRemovedTxt;

    @StringRes(R.string.undo_snackbar_stories_removed)
    String storiesRemovedTxt;

    @StringRes(R.string.undo_snackbar_story_removed_from_fav)
    String storyRemovedFromFavTxt;

    @StringRes(R.string.undo_snackbar_stories_removed_from_fav)
    String storiesRemovedFromFavTxt;

    @StringRes(R.string.undo_snackbar_cancel)
    String cancelTxt;

    Parcelable layoutManagerSavedState;

    public static FavoriteStoriesAdapter getAdapter() {
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
        loadData();
        restoreLayoutManagerPosition();
        initSwipeToDismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedSelectedItems = savedInstanceState;
            layoutManagerSavedState = savedInstanceState.getParcelable(SAVED_LAYOUT_MANAGER);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void loadData() {
        getLoaderManager().restartLoader(1, null, new LoaderManager.LoaderCallbacks<List<Story>>() {
            @Override
            public Loader<List<Story>> onCreateLoader(int id, Bundle args) {
                final AsyncTaskLoader<List<Story>> loader = new AsyncTaskLoader<List<Story>>(getActivity()) {
                    @Override
                    public List<Story> loadInBackground() {
                        return Story.selectFavorites();
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
                adapter = new FavoriteStoriesAdapter(data, new ClickListener() {
                    @Override
                    public void onItemClicked(View view, int position) {
                        if (MainActivity.getActionMode() != null) {
                            toggleSelection(position);
                        } else {
                            openFullStory(position);
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

    private void initSwipeToDismiss() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                MainActivity.destroyActionModeIfNeeded();
                if (direction == ItemTouchHelper.RIGHT) {
                    adapter.removeItem(viewHolder.getAdapterPosition(), REMOVE);
                    undoSnackbarShow(REMOVE);
                } else if (direction == ItemTouchHelper.LEFT) {
                    //adapter.invertFavoriteStatus(viewHolder.getAdapterPosition());
                    adapter.removeItem(viewHolder.getAdapterPosition(), REMOVE_FROM_FAV);
                    undoSnackbarShow(REMOVE_FROM_FAV);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void initRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new FavoriteStoriesAdapter());
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
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

    private void undoSnackbarShow(int RemoveSituation) {
        if (RemoveSituation == REMOVE) {
            Snackbar snackbar = Snackbar.make(recyclerView, adapter.getSelectedItemsCount() <= 1 ? storyRemovedTxt : storiesRemovedTxt, Snackbar.LENGTH_LONG)
                    .setAction(cancelTxt, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adapter.restoreRemovedItems();
                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            snackbar.show();
            adapter.startUndoRemoveTimer(3500);
        } else {
            Snackbar snackbar = Snackbar.make(recyclerView,
                                                adapter.getSelectedItemsCount() <= 1 ? storyRemovedFromFavTxt : storiesRemovedFromFavTxt,
                                                Snackbar.LENGTH_LONG)
                    .setAction(cancelTxt, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adapter.restoreRemovedFromFavItems();
                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(getContext(), R.color.primaryDark));
            snackbar.show();
            adapter.startUndoRemoveFromFavTimer(3500);
        }
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

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.cab, menu);
            MenuItem item = menu.findItem(R.id.menu_remove_from_fav);
            item.setVisible(true);
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
                    adapter.removeItems(adapter.getSelectedItems(), REMOVE);
                    undoSnackbarShow(REMOVE);
                    mode.finish();
                    return true;
                case R.id.menu_select_all:
                    adapter.selectAll(adapter.getItemCount());
                    MainActivity.getActionMode().setTitle(String.valueOf(adapter.getSelectedItemsCount()));
                    return true;
                case R.id.menu_remove_from_fav:
                    adapter.removeItems(adapter.getSelectedItems(), REMOVE_FROM_FAV);
                    undoSnackbarShow(REMOVE_FROM_FAV);
                    mode.finish();
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
        if (recyclerView != null) {
            outState.putParcelable(SAVED_LAYOUT_MANAGER, recyclerView.getLayoutManager().onSaveInstanceState());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (MainActivity.actionMode != null) {
            MainActivity.destroyActionModeIfNeeded();
        }
    }

    private void restoreLayoutManagerPosition() {
        if (layoutManagerSavedState != null && recyclerView != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
        }
        layoutManagerSavedState = null;
    }
}
