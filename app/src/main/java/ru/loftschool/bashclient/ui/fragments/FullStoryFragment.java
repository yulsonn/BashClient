package ru.loftschool.bashclient.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.BundleConstants;
import ru.loftschool.bashclient.ui.ToolbarInitialization;

@EFragment(R.layout.fragment_full_story)
public class FullStoryFragment extends Fragment {

    @ViewById(R.id.f_full_text)
    TextView fullText;

    @StringRes(R.string.message_added_to_fav)
    String addedToFav;

    @StringRes(R.string.message_deleted_from_fav)
    String deletedFromFav;

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
        MenuItem item = menu.findItem(R.id.menu_story_fav);
        item.setVisible(true);
        favStarColorize(item);
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
            fullText.setText(story.text);
            registerForContextMenu(fullText);
        }
    }

    private Story getCurrentStory() {
        Story story = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            long id = bundle.getLong(BundleConstants.ARG_ID);
            story = Story.select(id);
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
