package ru.loftschool.bashclient.utils;

import android.text.Html;

import java.util.List;

import retrofit.RetrofitError;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.rest.RestGeneralParams;
import ru.loftschool.bashclient.rest.RestService;
import ru.loftschool.bashclient.rest.model.StoryModel;

public class UpdateDataUtil {

    public static void loadData() throws RetrofitError{

        RestService restService = new RestService();
        List<StoryModel> stories;

        stories = restService.getStories(RestGeneralParams.SITE, RestGeneralParams.NAME, RestGeneralParams.POSTS_QTY);

        if (stories != null) {
            int maxNum = Story.getMaxNum();
            for (StoryModel story : stories) {
                String link = story.getLink();
                int storyNum = Integer.parseInt(link.substring(link.lastIndexOf("F") + 1, link.length()));
                if (storyNum > maxNum) {
                    String text = Html.fromHtml(story.getElementPureHtml()).toString().replace('\n', ' ');
                    String shortText = text.length() > 250 ? text.substring(0, 250) + "..." : text;
                    new Story(text, shortText, storyNum).save();
                }
            }
        }
    }
}
