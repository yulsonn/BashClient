package ru.loftschool.bashclient.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.adapters.FavoriteStoriesAdapter;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.BundleConstants;
import ru.loftschool.bashclient.ui.dialogs.DelFromFavoriteDialogFragment;
import ru.loftschool.bashclient.ui.listeners.ClickListener;
import ru.loftschool.bashclient.ui.ToolbarInitialization;

@EFragment(R.layout.fragment_favorite_stories)
public class FavoriteStoriesFragment extends Fragment{

    @ViewById(R.id.favorite_stories_container)
    RecyclerView recyclerView;

    @StringRes(R.string.frag_favorites_title)
    String title;

    private static FavoriteStoriesAdapter adapter;

    private List<Story> data;

    private ClickListener clickListener;

    public static FavoriteStoriesAdapter getAdapter() {
        return adapter;
    }

    @AfterViews
    void ready() {
        ToolbarInitialization.initToolbar(ToolbarInitialization.TOOLBAR_MAIN, (AppCompatActivity) getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
        initOnClickListener();
        data = Story.selectFavorites();
        adapter = new FavoriteStoriesAdapter(data, clickListener);
        initRecycleView();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(1, null, new LoaderManager.LoaderCallbacks<List<Story>>() {
            @Override
            public Loader<List<Story>> onCreateLoader(int id, Bundle args) {
                final AsyncTaskLoader<List<Story>> loader = new AsyncTaskLoader<List<Story>>(getActivity()) {
                    @Override
                    public List<Story> loadInBackground() {
                        return data;
                    }
                };
                loader.forceLoad();

                return loader;
            }

            @Override
            public void onLoadFinished(Loader<List<Story>> loader, List<Story> data) {
                //recyclerView.setAdapter(new FavoriteStoriesAdapter(Story.selectFavorites(), getContext()));
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<List<Story>> loader) {

            }
        });
    }

    private void initRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void initOnClickListener() {
        clickListener = new ClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                long id = data.get(position).getId();
                FullStoryFragment_ fullStoryFragment = new FullStoryFragment_();
                Bundle bundle = new Bundle();
                bundle.putLong(BundleConstants.ARG_ID, id);
                fullStoryFragment.setArguments(bundle);

                FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fullStoryFragment).addToBackStack(null).commit();
            }

            @Override
            public boolean onItemLongClicked(int position) {
                long id = data.get(position).getId();
                DelFromFavoriteDialogFragment dialog = new DelFromFavoriteDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putLong(BundleConstants.ARG_ID, id);
                bundle.putInt(BundleConstants.ARG_POSITION, position);
                dialog.setArguments(bundle);
                dialog.show(getActivity().getSupportFragmentManager(), "DelFromFav");
                return true;
            }
        };
    }
}
