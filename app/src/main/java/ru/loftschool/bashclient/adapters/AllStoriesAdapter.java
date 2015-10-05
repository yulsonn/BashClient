package ru.loftschool.bashclient.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.listeners.ClickListener;

public class AllStoriesAdapter extends RecyclerView.Adapter<AllStoriesAdapter.AllStoriesHolder>{

    private static final int TYPE_NOT_FAVORITE = 0;
    private static final int TYPE_FAVORITE = 1;

    private List<Story> stories;
    private ClickListener clickListener;

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

        return new AllStoriesHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(final AllStoriesHolder holder, int position) {
        final Story story = stories.get(position);
        holder.text.setText(story.shortText);
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

    public class AllStoriesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected CardView card;
        protected TextView text;
        protected ImageView favorite;

        private ClickListener listener;

        public AllStoriesHolder(View itemView, ClickListener listener) {
            super(itemView);
            this.listener = listener;
            card = (CardView) itemView.findViewById(R.id.card_view_all_stories);
            text = (TextView) itemView.findViewById(R.id.story_text);
            favorite = (ImageView) itemView.findViewById(R.id.story_favorite_flag);

            card.setOnClickListener(this);
            favorite.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(v, getAdapterPosition());
            }
        }
    }
}