package ru.loftschool.bashclient.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.Constants;
import ru.loftschool.bashclient.ui.ToolbarInitialization;
import ru.loftschool.bashclient.adapters.StoriesPagerAdapter;
import ru.loftschool.bashclient.utils.FullStoryUtil;

/**
 * Created by yulia on 09.04.16.
 */
public class FullStoriesTabsFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    private StoriesPagerAdapter adapter;
    private boolean allStoriesList;
    private int initialPosition;
    private int currentPosition;
    private Story currentStory;

    public FullStoriesTabsFragment() {
    }

    public static FullStoriesTabsFragment newInstance(int position, boolean allStories) {
        FullStoriesTabsFragment fragment = new FullStoriesTabsFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_POSITION, position);
        args.putBoolean(Constants.ARG_ALL_STORIES, allStories);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToolbarInitialization.initToolbar(ToolbarInitialization.TOOLBAR_ALT, (AppCompatActivity) getActivity());
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            initialPosition = getArguments().getInt(Constants.ARG_POSITION, 0);
            allStoriesList = getArguments().getBoolean(Constants.ARG_ALL_STORIES, true);
        }
        setCurrentStory(initialPosition);
        currentPosition = initialPosition;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pager, null);
        viewPager = (ViewPager) view.findViewById(R.id.stories_pager);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Инициализируем адаптер
        adapter = new StoriesPagerAdapter(getChildFragmentManager(), allStoriesList);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(initialPosition);
        viewPager.addOnPageChangeListener(this);
        setTitle();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // something that differs the Activity's menu
        MenuItem itemFav = menu.findItem(R.id.menu_story_fav);
        MenuItem itemShare = menu.findItem(R.id.menu_story_share);

        itemFav.setVisible(true);
        itemShare.setVisible(true);

        FullStoryUtil.favStarColorize(itemFav, currentStory);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_story_fav:
                FullStoryUtil.reverseFavorite(getActivity(), item, currentStory);
                if (!allStoriesList) {
                    adapter = new StoriesPagerAdapter(getChildFragmentManager(), allStoriesList);
                    viewPager.setAdapter(adapter);
                    viewPager.setCurrentItem(currentPosition);
                    setCurrentStory(currentPosition);
                }
                return true;
            case R.id.menu_story_share:
                FullStoryUtil.shareStory(getActivity(), currentStory);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        setCurrentStory(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void setCurrentStory(int position) {
        List<Story> stories = allStoriesList ? Story.selectAll() : Story.selectFavorites();
        if (stories.size() != 0 && position < stories.size()) {
            currentStory = stories.get(position);
        } else if (stories.size() != 0 && position >= stories.size()) {
            currentStory = stories.get(stories.size()-1);
        } else {
            currentStory = null;
            getActivity().onBackPressed();
        }
        setTitle();
    }

    private void setTitle() {
        if (currentStory != null) {
            String title = "#" + currentStory.storyNum;
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
        }
    }
}
