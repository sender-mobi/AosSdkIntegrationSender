package mobi.sender.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import mobi.sender.R;
import mobi.sender.model.ChatBased;
import mobi.sender.model.User;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;

/**
 * Created by Zver on 03.11.2016.
 */

public class AddSeveralContactRecAdapter extends RecyclerView.Adapter<AddSeveralContactRecAdapter.ViewHolder> {

    private Activity ctx;
    private List<ChatBased> userList;
    private CheckBoxListener listener;
    private List<String> listCheck = new ArrayList<>();

    public AddSeveralContactRecAdapter(Activity ctx, List<ChatBased> userList, CheckBoxListener listener) {
        this.ctx = ctx;
        this.userList = userList;
        this.listener = listener;
        listCheck.add(Storage.getInstance(ctx).getMyUserId());
    }

    @Override
    public AddSeveralContactRecAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_several_contacts, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final User user = (User) userList.get(position);

        holder.tvName.setText(user.getName());
        if (user.getChatPhoto() != null && !user.getChatPhoto().isEmpty()) {
            Tool.loadImage(ctx, user.getChatPhoto(), holder.ivAvatar, R.drawable.ic_acc_bg, true);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_acc_bg);
        }

        if (listCheck.contains(user.getUserId())) {
            holder.cbCheck.setChecked(true);
        } else {
            holder.cbCheck.setChecked(false);
        }

        holder.rlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.cbCheck.isChecked()) {
                    holder.cbCheck.setChecked(false);
                    listener.checkClick(user, false);
                    listCheck.remove(user.getUserId());
                } else {
                    holder.cbCheck.setChecked(true);
                    listener.checkClick(user, true);
                    listCheck.add(user.getUserId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setFilter(List<ChatBased> newModelList) {
        userList = newModelList;
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void setCheckBox(boolean b, String userId) {
        if (b){
            listCheck.add(userId);
        } else{
            if(listCheck.contains(userId)) listCheck.remove(userId);
        }
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public String[] getListCheck(){
        String []dsf = new String[listCheck.size()];
        listCheck.toArray(dsf);

        return dsf;
    }

    /**
     * Class to hold recycleView items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        ImageView ivAvatar;
        CheckBox cbCheck;
        RelativeLayout rlRoot;

        private ViewHolder(View v) {
            super(v);
            tvName = (TextView) v.findViewById(R.id.tv_name);
            ivAvatar = (ImageView) v.findViewById(R.id.iv_avatar);
            cbCheck = (CheckBox) v.findViewById(R.id.cb_check);
            rlRoot = (RelativeLayout) v.findViewById(R.id.rl_root);
        }
    }

    public interface CheckBoxListener {
        void checkClick(User user, boolean status);
    }
}
