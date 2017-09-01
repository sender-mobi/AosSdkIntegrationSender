package mobi.sender.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.R;
import mobi.sender.model.User;
import mobi.sender.tool.Tool;

public class AddSeveralContactAdapter extends BaseAdapter {

    private Activity ctx;
    private List<User> userList;
    private CheckBoxListener listener;
    private List<String> listCheck = new ArrayList<>();

    public AddSeveralContactAdapter(Activity ctx, List<User> userList, CheckBoxListener listener) {
        this.ctx = ctx;
        this.userList = userList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final User user = (User) getItem(position);

        convertView = LayoutInflater.from(ctx).inflate(R.layout.item_several_contacts, null);
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
        ImageView ivAvatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
        final CheckBox cbCheck = (CheckBox) convertView.findViewById(R.id.cb_check);
        RelativeLayout rlRoot = (RelativeLayout) convertView.findViewById(R.id.rl_root);

        tvName.setText(user.getName());
        if (user.getChatPhoto() != null && !user.getChatPhoto().isEmpty()) {
            Tool.loadImage(ctx, user.getChatPhoto(), ivAvatar, R.drawable.ic_acc_bg, true);
        } else{
            ivAvatar.setImageResource(R.drawable.ic_acc_bg);
        }

        if(listCheck.contains(user.getUserId())){
            cbCheck.setChecked(true);
        }else{
            cbCheck.setChecked(false);
        }

        rlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbCheck.isChecked()) {
                    cbCheck.setChecked(false);
                    listener.checkClick(user, false);
                    listCheck.remove(user.getUserId());
                } else {
                    cbCheck.setChecked(true);
                    listener.checkClick(user, true);
                    listCheck.add(user.getUserId());
                }
            }
        });

        return convertView;
    }

    public void setNewUsers(List<User> userList) {
        this.userList = userList;
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public interface CheckBoxListener {
        void checkClick(User user, boolean status);
    }
}
