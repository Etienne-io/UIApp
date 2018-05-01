package io.mapwize.uiapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.mapwize.mapwizeformapbox.model.Universe;

public class UniversesAdapter extends RecyclerView.Adapter<UniversesAdapter.UniverseItemViewHolder> {

    private List<Universe> mUniverses = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mListener;

    UniversesAdapter(Context context) {
        this.mContext = context;
    }

    void swapData(List<Universe> universes) {
        mUniverses = universes;
        notifyDataSetChanged();
    }

    void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public UniverseItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_item, parent, false);

        return new UniverseItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UniverseItemViewHolder holder, int position) {
        holder.itemView.setClickable(true);
        Universe universe = mUniverses.get(position);
        holder.textView.setText(universe.getName());
    }

    @Override
    public int getItemCount() {
        return mUniverses != null ? mUniverses.size() : 0;
    }

    class UniverseItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        UniverseItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getAdapterPosition();
                    if (mListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(mUniverses.get(adapterPosition));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Universe item);
    }
}
