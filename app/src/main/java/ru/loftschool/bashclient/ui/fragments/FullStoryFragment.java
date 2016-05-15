package ru.loftschool.bashclient.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.Constants;
import ru.loftschool.bashclient.ui.MyWebViewClient;
import ru.loftschool.bashclient.ui.ToolbarInitialization;
import ru.loftschool.bashclient.ui.listeners.LinkClickListener;
import ru.loftschool.bashclient.utils.FullStoryUtil;

@EFragment(R.layout.fragment_full_story)
public class FullStoryFragment extends Fragment {

    private final String MIME_TYPE = "text/html; charset=utf-8";
    private final String ENCODING = "UTF-8";
    private long storyId;
    private boolean openedFromLink;

    @ViewById(R.id.f_full_text)
    WebView fullText;

    public FullStoryFragment() {
    }

    public static FullStoryFragment newInstance(long storyId) {
        return newInstance(storyId, false);
    }

    public static FullStoryFragment newInstance(long storyId, boolean fromLink) {
        FullStoryFragment_ fragment = new FullStoryFragment_();
        Bundle args = new Bundle();
        args.putLong(Constants.ARG_ID, storyId);
        args.putBoolean(Constants.ARG_FROM_LINK, fromLink);
        fragment.setArguments(args);

        return fragment;
    }

    @AfterViews
    void ready() {
        ToolbarInitialization.initToolbar(ToolbarInitialization.TOOLBAR_ALT, (AppCompatActivity) getActivity());
        initContent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            storyId = args.getLong(Constants.ARG_ID);
            openedFromLink = args.getBoolean(Constants.ARG_FROM_LINK);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (openedFromLink) {
            setTitle();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // something that differs the Activity's menu
        if (openedFromLink) {
            MenuItem itemFav = menu.findItem(R.id.menu_story_fav);
            MenuItem itemShare = menu.findItem(R.id.menu_story_share);

            itemFav.setVisible(true);
            itemShare.setVisible(true);

            FullStoryUtil.favStarColorize(itemFav, getCurrentStory());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (openedFromLink) {
            switch (item.getItemId()) {
                case R.id.menu_story_fav:
                    FullStoryUtil.reverseFavorite(getActivity(), item, getCurrentStory());
                    return true;
                case R.id.menu_story_share:
                    FullStoryUtil.shareStory(getActivity(), getCurrentStory());
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void initContent() {
       Story story = getCurrentStory();
        if (story != null) {
            fullText.setWebViewClient(new MyWebViewClient(new LinkClickListener() {
                @Override
                public void onLinkClicked(long id) {
                    FullStoryFragment newStoryFragment = FullStoryFragment_.newInstance(id, true);
                    FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, newStoryFragment).addToBackStack(null).commit();
                }
            }));

            String storyText = "<font color='#585858' face='Calibri' line-height: '1.5'>" + story.text + "</font>";
            fullText.loadData(storyText, MIME_TYPE, ENCODING);
            registerForContextMenu(fullText);
        }
    }

    private Story getCurrentStory() {
        return  Story.selectById(storyId);
    }

    private void setTitle() {
        String title = null;
        Story story = getCurrentStory();
        if (story != null) {
            title = "#"+ story.storyNum;
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
    }
}
