package ru.loftschool.bashclient.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.MenuItem;
import android.widget.Toast;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;

/**
 * Created by yulia on 09.04.16.
 */
public class FullStoryUtil {

    public static void favStarColorize(MenuItem item, Story story) {
        if (story != null) {
            if(story.favorite) {
                item.setIcon(R.drawable.orange_star_48);
            } else {
                item.setIcon(R.drawable.white_star_48);
            }
        }
    }

    public static void reverseFavorite(Context context, MenuItem item, Story story) {
        story.favorite = !story.favorite;
        story.save();
        Toast.makeText(context, story.favorite ?
                context.getString(R.string.message_added_to_fav) :
                context.getString(R.string.message_deleted_from_fav),
                Toast.LENGTH_SHORT).show();
        favStarColorize(item, story);
    }

    public static void shareStory(Context context, Story story) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(context.getString(R.string.share_type));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }

        share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_site_address) + story.storyNum);

        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_text)));
    }
}
