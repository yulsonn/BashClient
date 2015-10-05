package ru.loftschool.bashclient.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "Stories")
public class Story extends Model {

    public static final String TEXT = "Text";
    public static final String SHORT_TEXT = "Short_text";
    public static final String FAVORITE = "Favorite";

    @Column(name = TEXT)
    public String text;

    @Column(name = SHORT_TEXT)
    public String shortText;

    @Column(name = FAVORITE)
    public boolean favorite;

    public Story() {
        super();
    }

    public Story(String text, String shortText) {
        super();
        this.text = text;
        this.shortText = shortText;
        this.favorite = false;
    }

    public Story(String text, String shortText, boolean favorite) {
        super();
        this.text = text;
        this.shortText = shortText;
        this.favorite = favorite;
    }

    public static List<Story> selectAll() {
        return new Select().from(Story.class).execute();
    }
    public static List<Story> selectFavorites() {
        return new Select().from(Story.class).where(FAVORITE + " = ?", true).execute();
    }

    public static Story select(long id) {
        return new Select().from(Story.class).where("id = ?", id).executeSingle();
    }

    public static void deleteAll() {
        new Delete().from(Story.class).execute();
    }
}
