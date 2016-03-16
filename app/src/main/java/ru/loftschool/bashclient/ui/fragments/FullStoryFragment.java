package ru.loftschool.bashclient.ui.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.BundleConstants;
import ru.loftschool.bashclient.ui.MyWebViewClient;
import ru.loftschool.bashclient.ui.ToolbarInitialization;
import ru.loftschool.bashclient.ui.listeners.LinkClickListener;

@EFragment(R.layout.fragment_full_story)
public class FullStoryFragment extends Fragment {

    private final String MIME_TYPE = "text/html; charset=utf-8";
    private final String ENCODING = "UTF-8";

    @ViewById(R.id.f_full_text)
    WebView fullText;

    @StringRes(R.string.message_added_to_fav)
    String addedToFav;

    @StringRes(R.string.message_deleted_from_fav)
    String deletedFromFav;

    @StringRes(R.string.share_text)
    String shareText;

    @StringRes(R.string.share_type)
    String shareType;

    @StringRes(R.string.share_site_address)
    String shareSite;

    @AfterViews
    void ready() {
        ToolbarInitialization.initToolbar(ToolbarInitialization.TOOLBAR_ALT, (AppCompatActivity) getActivity());
        initContent();
        setTitle();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // something that differs the Activity's menu
        MenuItem itemFav = menu.findItem(R.id.menu_story_fav);
        MenuItem itemShare = menu.findItem(R.id.menu_story_share);

        itemFav.setVisible(true);
        itemShare.setVisible(true);

        favStarColorize(itemFav);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
           case R.id.menu_story_fav:
               Story story = getCurrentStory();
               story.favorite = !story.favorite;
               story.save();
               Toast.makeText(getContext(), story.favorite ? addedToFav : deletedFromFav, Toast.LENGTH_SHORT).show();
               favStarColorize(item);
                return  true;
           case R.id.menu_story_share:
               Intent share = new Intent(Intent.ACTION_SEND);
               share.setType(shareType);
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                   share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
               } else {
                   share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
               }

               share.putExtra(Intent.EXTRA_TEXT, shareSite + getCurrentStory().storyNum);

               startActivity(Intent.createChooser(share, shareText));
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    private void favStarColorize(MenuItem item) {
        Story story = getCurrentStory();
        if (story != null) {
            if(story.favorite) {
                item.setIcon(R.drawable.orange_star_48);
            } else {
                item.setIcon(R.drawable.white_star_48);
            }
        }
    }

    private void initContent() {
       Story story = getCurrentStory();
        if (story != null) {
            fullText.setWebViewClient(new MyWebViewClient(new LinkClickListener() {
                @Override
                public void onLinkClicked(long id) {
                    FullStoryFragment_ newStoryFragment = new FullStoryFragment_();
                    Bundle bundle = new Bundle();
                    bundle.putLong(BundleConstants.ARG_ID, id);
                    newStoryFragment.setArguments(bundle);

                    FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, newStoryFragment).addToBackStack(null).commit();
                }
            }));

            String storyText = "<font color='#585858' face='Sans-serif'>" + story.text + "</font>";
            fullText.loadData(storyText, MIME_TYPE, ENCODING);
            registerForContextMenu(fullText);
        }
    }

    private Story getCurrentStory() {
        Story story = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            long id = bundle.getLong(BundleConstants.ARG_ID);
            story = Story.selectById(id);
        }
        return  story;
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
