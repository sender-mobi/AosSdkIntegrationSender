package mobi.sender.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.DelFromChatReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.User;
import mobi.sender.tool.CircleTransform;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.ui.ChatActivity;

/**
 * Created by Zver on 03.11.2016.
 */

public class MembersRecAdapter extends RecyclerView.Adapter<MembersRecAdapter.ViewHolder> {

    private final boolean isP2PChat;
    private Activity ctx;
    private String chatId;
    private List<User> listMembers;
    private Map<String, String> listRole;

    public MembersRecAdapter(Activity ctx, List<User> listMembers, String chatId, Map<String, String> listRole) {
        this.ctx = ctx;
        this.chatId = chatId;
        this.listMembers = listMembers;
        this.listRole = listRole;
        isP2PChat = Tool.isP2PChat(chatId);
    }

    @Override
    public MembersRecAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_members, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final User user = listMembers.get(position);
        Storage storage = Storage.getInstance(ctx);
        holder.tvMembers.setText(user.getName());
        if (!user.getChatPhoto().isEmpty()) {
            Picasso.with(ctx).load(user.getChatPhoto()).transform(new CircleTransform()).into(holder.ivMembers);
        } else {
            holder.ivMembers.setImageResource(R.drawable.ic_acc_bg);
        }

        if (!storage.getMyUserId().equals(user.getUserId()) && !storage.isOperChat(chatId)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(ctx, ChatActivity.class);
                    i.putExtra(ChatActivity.EXTRA_CHAT_ID, user.getChatId());
                    ctx.startActivity(i);
                }
            });
        }

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.confirmDilog(ctx, R.string.dlg_are_you_sure, R.string.dlg_remove_user_chat, new DialogUtils.OnConfirmListener() {
                    @Override
                    public void onYesClick() {
                        Bus.getInstance().post(new DelFromChatReq(chatId, user.getUserId(), new SyncEvent.SRespListener() {
                            @Override
                            public void onResponse(JSONObject data) {
                                ctx.finish();
                            }

                            @Override
                            public void onError(Exception e) {
                                ctx.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ctx, R.string.tst_error, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }));
                    }
                });
            }
        });

        if (!isP2PChat && !storage.isOperChat(chatId)) {
            holder.ivDelete.setVisibility(View.VISIBLE);
        } else {
            if (listRole.size() != 0) {
                String role = listRole.get(user.getUserId());
                holder.ivDelete.setVisibility("operator".equals(role) ? View.VISIBLE : View.GONE);

                String myUserId = storage.getMyUserId();
                if (myUserId.equals(user.getUserId())) {
                    holder.ivDelete.setVisibility(View.GONE);
                    holder.tvMembers.setText(R.string.msg_i);
                }
            }
        }

        if(storage.getMyUserId().equals(user.getUserId())){
            holder.ivDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listMembers.size();
    }


    /**
     * Class to hold recycleView items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvMembers;
        ImageView ivMembers;
        ImageView ivDelete;

        private ViewHolder(View v) {
            super(v);
            tvMembers = (TextView) v.findViewById(R.id.tv_members);
            ivMembers = (ImageView) v.findViewById(R.id.iv_members);
            ivDelete = (ImageView) v.findViewById(R.id.iv_delete);
        }
    }

}
