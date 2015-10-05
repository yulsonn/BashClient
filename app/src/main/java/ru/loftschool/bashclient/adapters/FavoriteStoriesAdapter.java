package ru.loftschool.bashclient.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.listeners.ClickListener;

public class FavoriteStoriesAdapter extends RecyclerView.Adapter<FavoriteStoriesAdapter.FavoriteStoriesHolder> {

    private List<Story> stories;
    private ClickListener clickListener;

    public FavoriteStoriesAdapter() {
    }

    public FavoriteStoriesAdapter(List<Story> stories, ClickListener clickListener) {
        this.stories = stories;
        this.clickListener = clickListener;
    }

    public List<Story> getStories() {
        return stories;
    }

    public void removeItem(int position) {
        stories.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public FavoriteStoriesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_favorite_stories, parent, false);
        return new FavoriteStoriesHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(FavoriteStoriesAdapter.FavoriteStoriesHolder holder, int position) {
        Story story = stories.get(position);
        holder.text.setText(Html.fromHtml(story.shortText));
    }

    @Override
    public int getItemCount() {
        return stories == null ? 0 : stories.size();
    }


    public static class FavoriteStoriesHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        protected CardView card;
        protected TextView text;

        private ClickListener listener;

        public FavoriteStoriesHolder(View itemView, ClickListener listener) {
            super(itemView);
            this.listener = listener;
            card = (CardView) itemView.findViewById(R.id.card_view_favorite_stories);
            text = (TextView) itemView.findViewById(R.id.fav_story_text);
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
