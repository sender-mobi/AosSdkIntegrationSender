package mobi.sender.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.R;
import mobi.sender.model.Country;

/**
 * Created by Zver on 03.11.2016.
 */

public class CountryAdapter2 extends RecyclerView.Adapter<CountryAdapter2.ViewHolder> {

    private List<Country> mListModel = new ArrayList<>();
    private Context mCtx;
    private CountryClickListener mListener;

    public CountryAdapter2(Context context, List<Country> modelList, CountryClickListener listener) {
        mListModel = modelList;
        mCtx = context;
        mListener = listener;
    }

    @Override
    public CountryAdapter2.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Country country = mListModel.get(position);
        holder.tvCountry.setText(country.getName());
        holder.tvCode.setText(country.getPrefix());
        Picasso.with(mCtx).load("https://s.sender.mobi/flag/" + country.getCode() + ".png").into(holder.ivFlag);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onCountryClickListener(country);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListModel.size();
    }

    public void setModels(List<Country> newCountries) {
        mListModel = newCountries;
        ((Activity) mCtx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Class to hold recycleView items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCountry;
        TextView tvCode;
        ImageView ivFlag;

        private ViewHolder(View v) {
            super(v);
            tvCountry = (TextView) v.findViewById(R.id.tv_country);
            tvCode = (TextView) v.findViewById(R.id.tv_code);
            ivFlag = (ImageView) v.findViewById(R.id.iv_flag);
        }
    }

    public interface CountryClickListener{
        void onCountryClickListener(Country country);
    }

}
