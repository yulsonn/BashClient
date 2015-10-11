package ru.loftschool.bashclient.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
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

    private static final int MENU_ITEM_NUM = 1;

    @ViewById(R.id.f_full_text)
    TextView fullText;

    @StringRes(R.string.context_menu_add_to_fav)
    String addToFav;

    @StringRes(R.string.context_menu_del_from_fav)
    String delFromFav;

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

    private void initContent() {
       Story story = getCurrentStory();
        if (story != null) {
            fullText.setText(story.text);
            registerForContextMenu(fullText);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Story story = getCurrentStory();
        if (story != null) {
            if (story.favorite) {
                menu.add(0, MENU_ITEM_NUM, 0, addToFav);
            } else {
                menu.add(0, MENU_ITEM_NUM, 0, delFromFav);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Story story = getCurrentStory();

            if (story != null) {
                String message = story.favorite ? deletedFromFav : addedToFav;
                story.favorite = (!story.favorite);
                story.save();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onContextItemSelected(item);
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
