package mobi.sender.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.LeaveChatReq;
import mobi.sender.event.SetChatOptionsReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.event.UpdateCtReq;
import mobi.sender.model.ChatBased;
import mobi.sender.model.User;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.ui.CardCompanyActivity;
import mobi.sender.ui.ChatActivity;

/**
 * Created by Zver on 03.11.2016.
 */

public class DialogRecAdapter extends RecyclerView.Adapter<DialogRecAdapter.ViewHolder> {

    private Context ctx;
    private List<ChatBased> chats;
    private final static String group_photo_url = "https://s.sender.mobi/form_images/sender/contact/contact/GroupChat_scaled.png";
    private boolean mIsJustComp;
    private OnItemClickListener mListener;

    public DialogRecAdapter(Context ctx, List<ChatBased> chats, boolean isJustComp, OnItemClickListener listener) {
        this.ctx = ctx;
        this.chats = chats;
        mIsJustComp = isJustComp;
        mListener = listener;
    }

    public void setChats(List<ChatBased> chats) {
        this.chats = chats;
        ((Activity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public DialogRecAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        final ChatBased c = chats.get(holder.getAdapterPosition());

        if (c.getName().isEmpty() || "".equals(c.getName().trim())) {
            holder.name.setText(ctx.getString(R.string.msg_you_removed_from_the_chat));
        } else {
            holder.name.setText(c.getName());
        }

        if ("".equals(c.getMessageText())) {
            holder.message.setVisibility(View.GONE);
        } else {
            holder.message.setText(c.getMessageText());
            Drawable img = null;

            if (c.getMessageText() != null) {
                if (c.getMessageText().contains(ctx.getString(R.string.stub_img))) {
                    img = ContextCompat.getDrawable(ctx, R.drawable.ic_camera);
                } else if (c.getMessageText().contains(ctx.getString(R.string.stub_file))) {
                    img = ContextCompat.getDrawable(ctx, R.drawable.ic_file_primary);
                } else if (c.getMessageText().contains(ctx.getString(R.string.stub_audio))) {
                    img = ContextCompat.getDrawable(ctx, R.drawable.ic_music);
                } else if (c.getMessageText().contains(ctx.getString(R.string.stub_video))) {
                    img = ContextCompat.getDrawable(ctx, R.drawable.ic_video);
                } else if (c.getMessageText().contains(ctx.getString(R.string.stub_location))) {
                    img = ContextCompat.getDrawable(ctx, R.drawable.ic_place_primary);
                } else if (c.getMessageText().contains(ctx.getString(R.string.stub_encr_msg)) || c.getMessageText().contains(ctx.getString(R.string.stub_encr_msg2))) {
                    img = ContextCompat.getDrawable(ctx, R.drawable.ic_encrypted);
                } else if (c.getMessageText().contains(ctx.getString(R.string.stub_form))) {
                    img = ContextCompat.getDrawable(ctx, R.drawable.ic_form);
                } else if (c.getMessageText().contains(ctx.getString(R.string.stub_sticker))) {
                    img = ContextCompat.getDrawable(ctx, R.drawable.ic_sticker);
                }
            }
            holder.message.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            holder.message.setCompoundDrawablePadding(16);
            holder.message.setVisibility(View.VISIBLE);
        }

        if (c.getCountUnread() > 0) {
            holder.counter.setVisibility(View.VISIBLE);
            holder.counter.setText(String.valueOf(c.getCountUnread()));
            holder.counter.setBackgroundResource(Storage.getInstance(ctx).isMute(c.getChatId()) ? R.drawable.shape_circle_alpha50 : R.drawable.shape_circle);

        } else {
            holder.counter.setVisibility(View.INVISIBLE);
        }

        Picasso.with(ctx).cancelRequest(holder.icon);
        if (c.getChatPhoto() == null || !c.getChatPhoto().startsWith("http") || group_photo_url.equals(c.getChatPhoto())) {
            holder.icon.setImageResource(Tool.getPlaceholder(c));
        } else {
            Tool.loadImage(ctx, c.getChatPhoto(), holder.icon, Tool.getPlaceholder(c), true);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               mListener.onClick(c);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mListener.onLongClick(c, holder.getAdapterPosition());
                return false;
            }
        });
    }

    public void removeAt(final int position) {
        chats.remove(position);
        ((Activity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    /**
     * Class to hold recycleView items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, message, counter;
        ImageView icon;

        private ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.cli_name);
            message = (TextView) v.findViewById(R.id.cli_message);
            counter = (TextView) v.findViewById(R.id.cli_counter);
            icon = (ImageView) v.findViewById(R.id.cli_icon);
        }
    }

    public interface OnItemClickListener {
        void onClick(ChatBased c);
        void onLongClick(ChatBased c, int pos);
    }
}
