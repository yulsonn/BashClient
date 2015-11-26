package ru.loftschool.bashclient.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.listeners.ClickListener;
import ru.loftschool.bashclient.utils.RemoveSituation;

public class FavoriteStoriesAdapter extends SelectableAdapter<FavoriteStoriesAdapter.FavoriteStoriesHolder> implements RemoveSituation{

    private static final long UNDO_TIMEOUT = 3600L;

    private List<Story> stories;
    private Map<Integer, Story> removedStoriesMap;
    private Map<Integer, Story> removedFromFavoritesStoriesMap;
    private ClickListener clickListener;
    private boolean multipleRemove = false;
    private Timer undoRemoveTimer;
    private Timer undoRemoveFromFavoritesTimer;
    private int lastPosition = -1;
    private Context context;

    public FavoriteStoriesAdapter() {
    }

    public FavoriteStoriesAdapter(List<Story> stories, ClickListener clickListener) {
        this.stories = stories;
        this.clickListener = clickListener;
    }

    public List<Story> getStories() {
        return stories;
    }

    @Override
    public FavoriteStoriesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_favorite_stories, parent, false);
        context = parent.getContext();

        return new FavoriteStoriesHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(FavoriteStoriesAdapter.FavoriteStoriesHolder holder, int position) {
        Story story = stories.get(position);
        holder.text.setText(Html.fromHtml(story.shortText));
        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return stories == null ? 0 : stories.size();
    }

    public void invertFavoriteStatus(int position) {
        Story story = stories.get(position);
        invertStoryFavoriteStatus(story);
        stories.remove(position);
        notifyItemRemoved(position);
    }

    public void invertStoryFavoriteStatus(Story story) {
        story.favorite = (!story.favorite);
        story.save();
    }


    public long getItemId(int position) {
        return stories.get(position).getId();
    }

    /* METHODS FOR REMOVING STORIES */

    private void removeStory(int position) {
        if (stories.get(position) != null) {
            stories.remove(position);
        }
    }

    public void removeItem(int position, int removeSituation) {
        if (!multipleRemove && removeSituation == REMOVE) {
            saveRemovedItem(position);
        } else if(!multipleRemove &&removeSituation == REMOVE_FROM_FAV) {
            saveRemovedFromFavItem(position);
        }

        removeStory(position);
        notifyItemRemoved(position);
    }

    public void removeItems(List<Integer> positions, int removeSituation) {
        if (positions.size() > 1) {
            multipleRemove = true;

            if (removeSituation == REMOVE) {
                saveRemovedItems(positions);
            } else if(removeSituation == REMOVE_FROM_FAV) {
                saveRemovedFromFavItems(positions);
            }
        }

        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        while (!positions.isEmpty()){
            if (positions.size() == 1) {
                removeItem(positions.get(0), removeSituation);
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                    count++;
                }

                if (count == 1) {
                    removeItem(positions.get(0), removeSituation);
                } else {
                    removeRange(positions.get(count - 1), count);
                }
                for (int i = 0; i < count; i++) {
                    positions.remove(0);
                }
            }
        }
        multipleRemove = false;
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int position = 0; position < itemCount; position++) {
            removeStory(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    private void completelyRemoveStoriesFromDB() {
        if (removedStoriesMap != null) {
            for (Map.Entry<Integer, Story> pair : removedStoriesMap.entrySet()) {
                pair.getValue().delete();
            }
            removedStoriesMap = null;
        }
    }

    /* METHODS FOR CHANGING FAVORITE STATE */

    private void completelyRemoveStoriesFromFavorites() {
        if (removedFromFavoritesStoriesMap != null) {
            for (Map.Entry<Integer, Story> pair : removedFromFavoritesStoriesMap.entrySet()) {
                invertStoryFavoriteStatus(pair.getValue());
            }
            removedFromFavoritesStoriesMap = null;
        }
    }

    /* METHODS FOR SAVING AND RESTORE REMOVED STORIES */

    private void saveRemovedItems(List<Integer> positions) {
        if (removedStoriesMap != null) {
            completelyRemoveStoriesFromDB();
        }
        removedStoriesMap = new TreeMap<>();
        for (int position : positions) {
            removedStoriesMap.put(position, stories.get(position));
        }
    }

    private void saveRemovedItem(int position) {
        if (removedStoriesMap != null) {
            completelyRemoveStoriesFromDB();
        }
        ArrayList<Integer> positions = new ArrayList<>(1);
        positions.add(position);
        saveRemovedItems(positions);
    }

    public void restoreRemovedItems() {
        stopUndoRemoveTimer();
        for (Map.Entry<Integer, Story> pair : removedStoriesMap.entrySet()){
            stories.add(pair.getKey(), pair.getValue());
            notifyItemInserted(pair.getKey());
        }
        removedStoriesMap = null;
    }

        /* METHODS FOR SAVING AND RESTORE REMOVED FROM FAVORITES STORIES */

    private void saveRemovedFromFavItems(List<Integer> positions) {
        if (removedFromFavoritesStoriesMap != null) {
            completelyRemoveStoriesFromFavorites();
        }
        removedFromFavoritesStoriesMap = new TreeMap<>();
        for (int position : positions) {
            removedFromFavoritesStoriesMap.put(position, stories.get(position));
        }
    }

    private void saveRemovedFromFavItem(int position) {
        if (removedFromFavoritesStoriesMap != null) {
            completelyRemoveStoriesFromFavorites();
        }
        ArrayList<Integer> positions = new ArrayList<>(1);
        positions.add(position);
        saveRemovedFromFavItems(positions);
    }

    public void restoreRemovedFromFavItems() {
        stopUndoRemoveTimer();
        for (Map.Entry<Integer, Story> pair : removedFromFavoritesStoriesMap.entrySet()){
            stories.add(pair.getKey(), pair.getValue());
            notifyItemInserted(pair.getKey());
        }
        removedFromFavoritesStoriesMap = null;
    }

    /* UndoRemoveTimer class*/

    private class UndoRemoveTimer extends TimerTask {
        @Override
        public void run() {
            undoRemoveTimer = null;
            completelyRemoveStoriesFromDB();
        }
    }

    public void startUndoRemoveTimer(long timeout) {
        stopUndoRemoveTimer();
        this.undoRemoveTimer = new Timer();
        this.undoRemoveTimer.schedule(new UndoRemoveTimer(), timeout > 0 ? timeout : UNDO_TIMEOUT);
    }

    private void stopUndoRemoveTimer() {
        if (this.undoRemoveTimer != null) {
            this.undoRemoveTimer.cancel();
            this.undoRemoveTimer = null;
        }
    }

    /* UndoRemoveFromFavTimer class*/

    private class UndoRemoveFromFavTimer extends TimerTask {
        @Override
        public void run() {
            undoRemoveFromFavoritesTimer = null;
            completelyRemoveStoriesFromFavorites();
        }
    }

    public void startUndoRemoveFromFavTimer(long timeout) {
        stopUndoRemoveFromFavTimer();
        this.undoRemoveFromFavoritesTimer = new Timer();
        this.undoRemoveFromFavoritesTimer.schedule(new UndoRemoveFromFavTimer(), timeout > 0 ? timeout : UNDO_TIMEOUT);
    }

    private void stopUndoRemoveFromFavTimer() {
        if (this.undoRemoveFromFavoritesTimer != null) {
            this.undoRemoveFromFavoritesTimer.cancel();
            this.undoRemoveFromFavoritesTimer = null;
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_up);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    /* ViewHolder class*/

    public static class FavoriteStoriesHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        protected CardView card;
        protected TextView text;
        protected View selectedOverlay;

        private ClickListener listener;

        public FavoriteStoriesHolder(View itemView, ClickListener listener) {
            super(itemView);
            this.listener = listener;
            card = (CardView) itemView.findViewById(R.id.card_view_favorite_stories);
            text = (TextView) itemView.findViewById(R.id.fav_story_text);
            selectedOverlay = itemView.findViewById(R.id.fav_stories_selected_overlay);

            card.setOnClickListener(this);
            card.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                return listener.onItemLongClicked(getAdapterPosition());
            }
            return false;
        }
    }
}
