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

public class LanguagesAdapter extends RecyclerView.Adapter<LanguagesAdapter.LanguageItemViewHolder> {


    private List<Locale> mLanguages = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mListener;

    LanguagesAdapter(Context context) {
        this.mContext = context;
    }

    void swapData(List<String> languages) {
        List<Locale> locales = new ArrayList<>();
        for (String l : languages) {
            locales.add(new Locale(l));
        }
        mLanguages = locales;
        notifyDataSetChanged();
    }

    void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public LanguageItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_item, parent, false);

        return new LanguageItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LanguageItemViewHolder holder, int position) {
        holder.itemView.setClickable(true);
        Locale language = mLanguages.get(position);
        holder.textView.setText(language.getDisplayLanguage().substring(0,1).toUpperCase() + language.getDisplayLanguage().substring(1));
    }

    @Override
    public int getItemCount() {
        return mLanguages != null ? mLanguages.size() : 0;
    }

    class LanguageItemViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        LanguageItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getAdapterPosition();
                    if (mListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(mLanguages.get(adapterPosition));
                    }
                }
            });
        }


    }

    public interface OnItemClickListener {
        void onItemClick(Locale item);
    }
}
