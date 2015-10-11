package ru.loftschool.bashclient.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import ru.loftschool.bashclient.ui.listeners.ClickListener;

@EFragment(R.layout.fragment_all_stories)
public class AllStoriesFragment extends Fragment{

    @ViewById(R.id.all_stories_container)
    RecyclerView recyclerView;

    @ViewById(R.id.main_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @StringRes(R.string.frag_all_title)
    String title;

    private AllStoriesAdapter adapter;

    private List<Story> data;

    private ClickListener clickListener;

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    @AfterViews
    void ready() {
        ToolbarInitialization.initToolbar(ToolbarInitialization.TOOLBAR_MAIN, (AppCompatActivity) getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
        initOnClick();
        data = Story.selectAll();
        adapter = new AllStoriesAdapter(data, clickListener);
        initRecycleView();
        initSwipeRefresh();
    }

    private void initSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.primaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RefreshDataService_.intent(getContext()).start();
            }
        });
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
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void initOnClick() {
        clickListener = new ClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                switch (view.getId()){
                    case R.id.story_favorite_flag:
                        Story story = adapter.getStories().get(position);
                        story.favorite = !story.favorite;
                        story.save();
                        adapter.notifyItemChanged(position);
                        break;
                    case R.id.card_view_all_stories:
                        long id = data.get(position).getId();
                        FullStoryFragment_ fullStoryFragment = new FullStoryFragment_();
                        Bundle bundle = new Bundle();
                        bundle.putLong(BundleConstants.ARG_ID, id);
                        fullStoryFragment.setArguments(bundle);

                        FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fullStoryFragment).addToBackStack(null).commit();
                        break;
                }
            }

            @Override
            public boolean onItemLongClicked(int position) {
                return false;
            }
        };
    }
}
