package ru.loftschool.bashclient.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

@Table(name = "Stories")
public class Story extends Model {

    public static final String TEXT = "Text";
    public static final String SHORT_TEXT = "Short_text";
    public static final String FAVORITE = "Favorite";
    public  static final String STORY_NUM = "Story_number";

    @Column(name = TEXT)
    public String text;

    @Column(name = SHORT_TEXT)
    public String shortText;

    @Column(name = STORY_NUM)
    public int storyNum;

    @Column(name = FAVORITE)
    public boolean favorite;

    public Story() {
        super();
    }

    public Story(String text, String shortText, Integer storyNum) {
        super();
        this.text = text;
        this.shortText = shortText;
        this.storyNum = storyNum;
        this.favorite = false;
    }

    public Story(String text, String shortText, Integer storyNum, boolean favorite) {
        super();
        this.text = text;
        this.shortText = shortText;
        this.storyNum = storyNum;
        this.favorite = favorite;
    }

    public static List<Story> selectAll() {
        return new Select().from(Story.class).orderBy(STORY_NUM + " DESC").execute();
    }
    public static List<Story> selectFavorites() {
        return new Select().from(Story.class).where(FAVORITE + " = ?", true).orderBy(STORY_NUM + " DESC").execute();
    }

    public static Story selectById(long id) {
        return new Select().from(Story.class).where("id = ?", id).executeSingle();
    }

    public static Story selectByNum(int num) {
        return new Select().from(Story.class).where(STORY_NUM + " = ?", num).executeSingle();
    }

    public static void deleteAll() {
        new Delete().from(Story.class).execute();
    }

    public static void deleteByNum(int num){
        new Delete().from(Story.class).where(STORY_NUM + " = ?", num).execute();
    }

    public static int getMaxNum() {
        if (selectAll().size() != 0) {
            return ((Story) new Select().from(Story.class).orderBy(STORY_NUM + " DESC").executeSingle()).storyNum;
        } else {
            return 0;
        }
    }
}
