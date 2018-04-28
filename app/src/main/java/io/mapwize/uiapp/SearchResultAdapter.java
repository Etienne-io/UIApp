package io.mapwize.uiapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchItemViewHolder> {


    private List mSearchSuggestions = new ArrayList<>();
    private Context mContext;
    private Listener mListener;

    SearchResultAdapter(Context context) {
        this.mContext = context;
    }

    void swapData(List searchSuggestions) {
        mSearchSuggestions = searchSuggestions;
        notifyDataSetChanged();
        Log.i("Debug", "Swap data " + mSearchSuggestions.size());
    }

    void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public SearchItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i("Debug", "create view holder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_item, parent, false);

        return new SearchItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchItemViewHolder holder, int position) {
        Log.i("Debug", "bind view holder");
        holder.itemView.setClickable(true);
    }

    @Override
    public int getItemCount() {
        return mSearchSuggestions != null ? mSearchSuggestions.size() : 0;
    }

    class SearchItemViewHolder extends RecyclerView.ViewHolder {

        SearchItemViewHolder(View itemView) {
            super(itemView);
            Log.i("Debug", "touhou");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getAdapterPosition();
                    if (mListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                        mListener.onItemSelected(mSearchSuggestions.get(adapterPosition));
                    }
                }
            });
        }

    }

    public interface Listener {
        void onItemSelected(Object item);
    }
}
