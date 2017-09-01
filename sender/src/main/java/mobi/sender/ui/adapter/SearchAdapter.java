package mobi.sender.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.SyncEvent;
import mobi.sender.event.UpdateCtReq;
import mobi.sender.model.ChatBased;
import mobi.sender.model.Dialog;
import mobi.sender.model.User;
import mobi.sender.tool.utils.AnimationUtils;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.ui.CardCompanyActivity;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.SearchActivity;

public class SearchAdapter extends ArrayAdapter<String> {

    private Context ctx;
    private List<ChatBased> userList;
    private int size;

    public SearchAdapter(Context ctx, List<ChatBased> userList) {
        super(ctx, R.layout.item_search);
        this.ctx = ctx;
        this.userList = userList;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_search, parent, false);

        if (userList.size() != 0) {
            final ChatBased c = userList.get(position);


            TextView tvName = (TextView) rowView.findViewById(R.id.tv_name);
            ImageView ivAvatar = (ImageView) rowView.findViewById(R.id.iv_avatar);
            View divider = rowView.findViewById(R.id.v_divider);

            tvName.setText(c.getName());
            if (c.getChatPhoto() != null && !c.getChatPhoto().isEmpty()) {
                Tool.loadImage(ctx, c.getChatPhoto(), ivAvatar, R.drawable.ic_acc_bg, true);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_acc_bg);
            }

            if (position == size) {
                divider.setVisibility(View.VISIBLE);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   /* if (position < size) {
                        Intent intent = new Intent(ctx, ChatActivity.class);
                        intent.putExtra(ChatActivity.EXTRA_CHAT_ID, c.getChatId());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    } else {
                        Bus.getInstance().post(new UpdateCtReq(c.getChatId(), c.getName(), true));

                        User user = (User) c;

                        Storage.getInstance(getContext()).saveUser(user);
                        ((SearchActivity) ctx).finish();

                        Intent intent = new Intent(ctx, ChatActivity.class);
                        intent.putExtra(ChatActivity.EXTRA_CHAT_ID, c.getChatId());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    }*/
                    if (position > size) {
                        Bus.getInstance().post(new UpdateCtReq(c.getChatId(), c.getName(), true, new SyncEvent.SRespListener() {
                            @Override
                            public void onResponse(JSONObject data) {
                                User user = (User) c;

                                Storage.getInstance(getContext()).saveUser(user);
                                ((SearchActivity) ctx).finish();
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        }));
                    }
                    open(c);

                    AnimationUtils.clickAnimation(view);
                    ((SearchActivity) ctx).finish();
                }
            });
        }

        return rowView;
    }

    private void open(ChatBased c) {
        if (c instanceof User) {
            User u = (User) c;
            if (u.isCompany() && u.getCountUnread() == 0) {

                Tool.log("*** photo = "+c.getChatPhoto());
                Storage storage = Storage.getInstance(ctx);
                if (!storage.isUserExists(u.getUserId())) {
                    u.setName(c.getName());
                    u.setChatPhoto(c.getChatPhoto());
                    u.setChatId(c.getChatId());
                    u.setCompany(u.isCompany());
                    storage.saveUser(u);
                }

                ctx.startActivity(new Intent(ctx, CardCompanyActivity.class).putExtra(CardCompanyActivity.CHAT_ID, c.getChatId()));
            } else {
                ctx.startActivity(new Intent(ctx, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, c.getChatId()));
            }
        } else {
            ctx.startActivity(new Intent(ctx, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, c.getChatId()));
        }
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setUsers(List<ChatBased> userList) {
        this.userList = userList;
        ((SearchActivity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void setLocalSize(int size) {
        this.size = size;
    }
}
