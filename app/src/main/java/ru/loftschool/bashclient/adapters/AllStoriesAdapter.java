package ru.loftschool.bashclient.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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

public class AllStoriesAdapter extends SelectableAdapter<AllStoriesAdapter.AllStoriesHolder>{

    private static final int TYPE_NOT_FAVORITE = 0;
    private static final int TYPE_FAVORITE = 1;
    private static final long UNDO_TIMEOUT = 3600L;

    private List<Story> stories;
    private Map<Integer, Story> removedStoriesMap;
    private ClickListener clickListener;
    private boolean multipleRemove = false;
    private Timer undoRemoveTimer;
    private int lastPosition = -1;
    private Context context;

    public AllStoriesAdapter() {
    }

    public AllStoriesAdapter(List<Story> stories, ClickListener clickListener) {
        this.stories = stories;
        this.clickListener = clickListener;
    }

    public List<Story> getStories() {
        return stories;
    }

    @Override
    public AllStoriesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final int layout = viewType == TYPE_NOT_FAVORITE ? R.layout.list_item_all_stories : R.layout.list_item_all_stories_favorite;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        context = parent.getContext();

        return new AllStoriesHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(final AllStoriesHolder holder, int position) {
        final Story story = stories.get(position);
        holder.text.setText(story.shortText);
        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);

        //setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return stories == null ? 0 : stories.size();
    }

    @Override
    public int getItemViewType(int position) {
        final Story story = stories.get(position);
        return story.favorite ? TYPE_FAVORITE : TYPE_NOT_FAVORITE;
    }

    private void removeStory(int position) {
        if (stories.get(position) != null) {
            stories.remove(position);
        }
    }

    public void removeItem(int position) {
        if (!multipleRemove) {
            saveRemovedItem(position);
        }
        removeStory(position);
        notifyItemRemoved(position);
    }

    public void removeItems(List<Integer> positions) {
        if (positions.size() > 1) {
            multipleRemove = true;
            saveRemovedItems(positions);
        }

        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        while (!positions.isEmpty()){
            if (positions.size() == 1) {
                removeItem(positions.get(0));
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                    count++;
                }

                if (count == 1) {
                    removeItem(positions.get(0));
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

    public void invertFavoriteStatus(int position) {
        Story story = stories.get(position);
        story.favorite = (!story.favorite);
        story.save();
        notifyItemChanged(position);
    }

    public long getItemId(int position) {
        return stories.get(position).getId();
    }

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
        stopUndoTimer();
        for (Map.Entry<Integer, Story> pair : removedStoriesMap.entrySet()){
            stories.add(pair.getKey(), pair.getValue());
            notifyItemInserted(pair.getKey());
        }
        removedStoriesMap = null;
    }

    public void startUndoTimer(long timeout) {
        stopUndoTimer();
        this.undoRemoveTimer = new Timer();
        this.undoRemoveTimer.schedule(new UndoTimer(), timeout > 0 ? timeout : UNDO_TIMEOUT);
    }

    private void stopUndoTimer() {
        if (this.undoRemoveTimer != null) {
            this.undoRemoveTimer.cancel();
            this.undoRemoveTimer = null;
        }
    }

    /* UndoTimer class*/

    private class UndoTimer extends TimerTask {
        @Override
        public void run() {
            undoRemoveTimer = null;
            completelyRemoveStoriesFromDB();
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

    public static class AllStoriesHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        protected CardView card;
        protected TextView text;
        protected ImageView favorite;
        protected View selectedOverlay;

        private ClickListener listener;

        public AllStoriesHolder(View itemView, ClickListener listener) {
            super(itemView);
            this.listener = listener;
            card = (CardView) itemView.findViewById(R.id.card_view_all_stories);
            text = (TextView) itemView.findViewById(R.id.story_text);
            favorite = (ImageView) itemView.findViewById(R.id.story_favorite_flag);
            selectedOverlay = itemView.findViewById(R.id.all_stories_selected_overlay);

            card.setOnClickListener(this);
            card.setOnLongClickListener(this);
            favorite.setOnClickListener(this);
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