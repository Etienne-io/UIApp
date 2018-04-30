package io.mapwize.uiapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import io.mapwize.mapwizeformapbox.model.Place;
import io.mapwize.mapwizeformapbox.model.PlaceList;
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
            holder.floorView.setVisibility(View.GONE);
        }

        if (suggestionItem instanceof Place) {
            Place place = (Place) suggestionItem;
            Translation translation = place.getTranslation("en");
            holder.titleView.setText(translation.getTitle());
            holder.leftIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_location_on_black_24dp));
            holder.floorView.setVisibility(View.VISIBLE);
            NumberFormat nf = new DecimalFormat("###.###");
            holder.floorView.setText(String.format("Floor %1$s", nf.format(place.getFloor())));
        }

        if (suggestionItem instanceof PlaceList) {
            PlaceList placeList = (PlaceList) suggestionItem;
            Translation translation = placeList.getTranslation("en");
            holder.titleView.setText(translation.getTitle());
            holder.leftIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_menu_black_24dp));
            holder.floorView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mSearchSuggestions != null ? mSearchSuggestions.size() : 0;
    }

    class SearchItemViewHolder extends RecyclerView.ViewHolder {

        ImageView leftIcon;
        TextView titleView;
        TextView floorView;

        SearchItemViewHolder(View itemView) {
            super(itemView);
            leftIcon = itemView.findViewById(R.id.suggestions_item_icon);
            titleView = itemView.findViewById(R.id.suggestions_item_title);
            floorView = itemView.findViewById(R.id.suggestions_item_floor);

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
