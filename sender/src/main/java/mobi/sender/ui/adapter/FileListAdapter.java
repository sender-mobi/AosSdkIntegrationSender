package mobi.sender.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mobi.sender.R;
import mobi.sender.tool.Tool;

public class FileListAdapter extends BaseAdapter {

    private List<File> files = new ArrayList<File>();
    private Activity act;

    public FileListAdapter(final Activity act) {
        this.act = act;
    }

    public void setFiles(List<File> files) {
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.isDirectory() == rhs.isDirectory()) {
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    return lhs.isDirectory() ? -1 : 1;
                }
            }
        });
        this.files = files;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public File getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final File f = getItem(position);
        if (f == null) return null;
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(act).inflate(R.layout.item_list_file, null);
            holder = new ViewHolder();
            holder.tvFileName = (TextView) convertView.findViewById(R.id.tvFname);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvFileName.setText(f.getName());
        String name = f.getName().toLowerCase();
        if ((name.endsWith("png") || name.endsWith("jpg") || name.endsWith("jpeg"))) {
            Tool.loadImage(act, f.getAbsolutePath(), holder.ivIcon, R.drawable.file, true);
        } else {
            holder.ivIcon.setImageResource(f.isDirectory() ? R.drawable._folder : R.drawable.file);
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tvFileName;
        ImageView ivIcon;
    }
}