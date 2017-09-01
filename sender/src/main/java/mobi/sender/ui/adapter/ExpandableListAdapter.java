package mobi.sender.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.LeaveChatReq;
import mobi.sender.model.ChatBased;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.MainActivity;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context ctx;
    private List<String> listDataHeader;
    private HashMap<String, ArrayList<ChatBased>> listDataChild;
    private List<String> listIconHeader;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, ArrayList<ChatBased>> listChildData, List<String> listIconHeader) {
        this.ctx = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        this.listIconHeader = listIconHeader;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return listDataChild.get(listDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final ChatBased model = (ChatBased) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.operator_child_item, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.opc_child_name);
        txtListChild.setText(model.getName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_CHAT_ID, model.getChatId());
                ctx.startActivity(intent);
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                DialogUtils.confirmDilog(((Activity) ctx), 0, R.string.dlg_leave_chat, new DialogUtils.OnConfirmListener() {
                    @Override
                    public void onYesClick() {
                        Bus.getInstance().post(new LeaveChatReq(model.getChatId()));
                        ctx.startActivity(new Intent(ctx, MainActivity.class));
                    }
                });
                return false;
            }
        });

        String headerTitle = (String) getGroup(groupPosition);
        int countUnread = listDataChild.get(headerTitle).get(childPosition).getCountUnread();

        TextView tvBadge = (TextView) convertView.findViewById(R.id.opc_badge);
        if(countUnread != 0) {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(countUnread + "");
        } else {
            tvBadge.setVisibility(View.GONE);
        }

        Storage storage = Storage.getInstance(ctx);
        if(!storage.isUserInDialog(model.getChatId(), storage.getMyUserId())){
            txtListChild.setPaintFlags(txtListChild.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            txtListChild.setPaintFlags(0);
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listDataChild.get(listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return listDataChild.keySet().size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.operator_group_item, null);
        }

        ((TextView) convertView.findViewById(R.id.opc_group_name)).setText(headerTitle);
        Tool.loadImage(ctx, listIconHeader.get(groupPosition), ((ImageView) convertView.findViewById(R.id.opc_group_image)), R.drawable.ic_group_bg, true);

        int countUnread = 0;
        List<ChatBased> kk = listDataChild.get(headerTitle);
        for(int i = 0; i<kk.size(); i++){
            if(kk.get(i).getCountUnread() > 0) {
                countUnread++;
            }
        }

        TextView tvBadge = (TextView) convertView.findViewById(R.id.opc_badge);
        if(countUnread != 0) {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(countUnread + "");
        } else {
            tvBadge.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}