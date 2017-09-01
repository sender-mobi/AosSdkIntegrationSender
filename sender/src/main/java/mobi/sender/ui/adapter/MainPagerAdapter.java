package mobi.sender.ui.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.R;

/**
 * Created by vp on 18.07.16.
 */
public class MainPagerAdapter extends PagerAdapter {

    private ArrayList<View> views = new ArrayList<>();
    private Activity act;
    private List<Integer> resList = new ArrayList<>();

    public MainPagerAdapter(Activity act, List<Integer> resList) {
        this.act = act;
        this.resList = resList;
    }

    public void addPage(View v) {
        views.add(v);
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void updatePage(View v, int pos) {
        views.set(pos, v);
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = views.get(position);
        container.addView(v, 0);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    public View getTabView(int position, int count) {
        View v = LayoutInflater.from(act).inflate(R.layout.item_badge, null);
        TextView tv = (TextView) v.findViewById(R.id.tv_badge_text);
        tv.setVisibility(View.VISIBLE);
        if (count != 0) {
            tv.setText(count + "");
        } else {
            tv.setVisibility(View.GONE);
        }
        ImageView img = (ImageView) v.findViewById(R.id.iv_icon);
        if(position <= resList.size()-1) {
            img.setImageResource(resList.get(position));
        }
        return v;
    }

    public void clear() {
        views.clear();
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
