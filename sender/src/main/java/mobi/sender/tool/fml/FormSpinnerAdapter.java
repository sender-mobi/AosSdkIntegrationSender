package mobi.sender.tool.fml;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.R;
import mobi.sender.tool.Tool;

/**
 * Created by user2 on 14.07.14.
 */
public class FormSpinnerAdapter extends BaseAdapter {

    private float textSize;
    private Activity activity;

    private List<String> items;

    private List<String> tags;

    public String sel_value;

    public FormSpinnerAdapter(Activity activity, List<String> items, float textSize) {
        this.textSize = textSize;
        this.activity = activity;
        if (items == null) {
            this.items = new ArrayList<String>();
        } else {
            this.items = items;
        }
        tags = new ArrayList<String>();
    }

    public void addItem(String item, String tag) {
        this.items.add(item);
        this.tags.add(tag);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public String getTagByPosition(int position) {
        try {
            return tags.get(position);
        } catch (IndexOutOfBoundsException ex) {
            Tool.log("index out of bounds exception in tags");
        }
        return "";
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public String getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_spinner_dropdown_form_static, null);
        }
        TextView textItem = (TextView) convertView.findViewById(R.id.tvTitle);
        textItem.setText(getItem(position));
        textItem.setTextSize(textSize);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.item_spinner_dropdown_form, null);
        }
        TextView textItem = (TextView) convertView.findViewById(R.id.tvTitle);
        textItem.setText(getItem(position));
        textItem.setTextSize(textSize);
        return convertView;
    }
}
