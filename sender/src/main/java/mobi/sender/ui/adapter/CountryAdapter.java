package mobi.sender.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import mobi.sender.R;
import mobi.sender.model.Country;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vp
 * on 20.05.16.
 */
public class CountryAdapter extends BaseAdapter {

    private Context ctx;
    private List<Country> countries;

    public CountryAdapter(Context ctx) {
        this.ctx = ctx;
        countries = new ArrayList<>();
    }

    public CountryAdapter(Context ctx, List<Country> countries) {
        this.ctx = ctx;
        this.countries = countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
        ((Activity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return countries.size();
    }

    @Override
    public Object getItem(int position) {
        return countries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Country c = (Country) getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
        convertView = LayoutInflater.from(ctx).inflate(R.layout.country_spinner_item, null);
        holder = new ViewHolder();
        holder.name = (TextView) convertView.findViewById(R.id.country_name);
        convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(c.getName());
        return convertView;
    }

    private static class ViewHolder {
        TextView name;
    }
}
