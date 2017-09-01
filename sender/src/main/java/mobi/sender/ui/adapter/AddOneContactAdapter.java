package mobi.sender.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import mobi.sender.R;
import mobi.sender.model.ChatBased;
import mobi.sender.model.Dialog;
import mobi.sender.model.User;
import mobi.sender.tool.Tool;
import mobi.sender.ui.window.AddOneUserWindow;

public class AddOneContactAdapter extends BaseAdapter {

    private Context ctx;
    private List<ChatBased> listMembers;

    public AddOneContactAdapter(Context ctx, List<ChatBased> listMembers) {
        this.ctx = ctx;
        this.listMembers = listMembers;
    }

    @Override
    public int getCount() {
        return listMembers.size();
    }

    @Override
    public Object getItem(int position) {
        return listMembers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(ctx).inflate(R.layout.item_one_contacts, null);

        TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
        ImageView ivAvatar = (ImageView) convertView.findViewById(R.id.iv_avatar);

        if (getItem(position) instanceof User) {
            User user = (User) getItem(position);
            tvName.setText(user.getName());
            if (!user.getChatPhoto().isEmpty()) {
                Tool.loadImage(ctx, user.getChatPhoto(), ivAvatar, R.drawable.ic_acc_bg, true);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_acc_bg);
            }

        } else if (getItem(position) instanceof Dialog) {
            Dialog d = (Dialog) getItem(position);
            tvName.setText(d.getName());
            if (!d.getChatPhoto().isEmpty()) {
                Tool.loadImage(ctx, d.getChatPhoto(), ivAvatar, R.drawable.ic_group_bg, true);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_group_bg);
            }
        } else {
            //it's block just for create group item
            if (position == 0 && AddOneUserWindow.CREATE_GROUP_CHAT.equals(listMembers.get(position).getChatId())) {
                ivAvatar.setImageResource(R.drawable.ic_create_group_chat);
                tvName.setText(ctx.getString(R.string.msg_create_group_dialog));
            }
        }

        return convertView;
    }

    public void setNewUsers(List<ChatBased> userList) {
        this.listMembers = userList;
        ((Activity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
