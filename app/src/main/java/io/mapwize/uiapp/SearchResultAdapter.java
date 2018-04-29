package io.mapwize.uiapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.mapwize.mapwizeformapbox.model.Translation;
import io.mapwize.mapwizeformapbox.model.Venue;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchItemViewHolder> {


    private List mSearchSuggestions = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mListener;

    SearchResultAdapter(Context context) {
        this.mContext = context;
    }

    void swapData(List searchSuggestions) {
        mSearchSuggestions = searchSuggestions;
        notifyDataSetChanged();
    }

    void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public SearchItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_item, parent, false);

        return new SearchItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchItemViewHolder holder, int position) {
        holder.itemView.setClickable(true);

        Object suggestionItem = mSearchSuggestions.get(position);

        if (suggestionItem instanceof Venue) {
            Venue venue = (Venue) suggestionItem;
            Translation translation = venue.getTranslation("en");
            holder.titleView.setText(translation.getTitle());
            holder.leftIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_domain_black_24dp));
        }
    }

    @Override
    public int getItemCount() {
        return mSearchSuggestions != null ? mSearchSuggestions.size() : 0;
    }

    class SearchItemViewHolder extends RecyclerView.ViewHolder {

        ImageView leftIcon;
        TextView titleView;

        SearchItemViewHolder(View itemView) {
            super(itemView);
            leftIcon = itemView.findViewById(R.id.suggestions_item_icon);
            titleView = itemView.findViewById(R.id.suggestions_item_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getAdapterPosition();
                    if (mListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(mSearchSuggestions.get(adapterPosition));
                    }
                }
            });
        }


    }

    public interface OnItemClickListener {
        void onItemClick(Object item);
    }
}
