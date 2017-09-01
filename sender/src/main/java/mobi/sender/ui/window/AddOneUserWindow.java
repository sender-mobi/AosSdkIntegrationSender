package mobi.sender.ui.window;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.R;
import mobi.sender.model.ChatBased;
import mobi.sender.tool.utils.KeyboardUtils;
import mobi.sender.ui.adapter.AddOneContactAdapter;

public class AddOneUserWindow {

    private final boolean visibleGroupChat;
    private Activity mAct;
    private List<ChatBased> chatBasedList;
    private Dialog dialog;
    private OnSelectUserListener listener;
    private EditText etSearch;
    private CheckBox cbSearch;
    private AddOneContactAdapter adapter;
    public static String CREATE_GROUP_CHAT = "create_group_chat";

    public AddOneUserWindow(Activity mAct, List<ChatBased> chatBasedList, boolean visibleGroupChat, OnSelectUserListener listener) {
        this.mAct = mAct;
        this.chatBasedList = chatBasedList;
        this.listener = listener;
        this.visibleGroupChat = visibleGroupChat;
    }

    public void show() {
        dialog = new Dialog(mAct, R.style.AppTheme);
        dialog.setContentView(getCtListView());
        dialog.show();
    }

    private View getCtListView() {
        LayoutInflater inflater = (LayoutInflater) mAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View currentView = inflater.inflate(R.layout.dialog_select_user, null);

        if(visibleGroupChat){
            addCreateGroupChatItem();
        }

        //make adapter
        adapter = new AddOneContactAdapter(mAct, chatBasedList);
        final ListView listView = (ListView) currentView.findViewById(R.id.lv_contacts);
        listView.setAdapter(adapter);

        //back press
        currentView.findViewById(R.id.fl_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //on item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listener.onSelect((ChatBased) adapter.getItem(i), dialog);
            }
        });

        cbSearch = (CheckBox) currentView.findViewById(R.id.cb_search);
        final TextView tvTitle = (TextView) currentView.findViewById(R.id.tv_title);
        etSearch = (EditText) currentView.findViewById(R.id.et_search);
        FrameLayout flSearch = (FrameLayout) currentView.findViewById(R.id.fl_search);
        flSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbSearch.isChecked()) {
                    cbSearch.setChecked(false);
                    tvTitle.setVisibility(View.VISIBLE);
                    etSearch.setVisibility(View.GONE);
                    KeyboardUtils.visibleKeyboard(false, etSearch, mAct);
                    etSearch.setText("");
                } else {
                    cbSearch.setChecked(true);
                    tvTitle.setVisibility(View.GONE);
                    etSearch.setVisibility(View.VISIBLE);
                    etSearch.requestFocus();
                    KeyboardUtils.visibleKeyboard(true, etSearch, mAct);
                }
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                filter(text.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return currentView;
    }

    private void addCreateGroupChatItem(){
        ChatBased c = new ChatBased();
        c.setChatId(CREATE_GROUP_CHAT);
        chatBasedList.add(0, c);
    }

    private void filter(String str) {
        List<ChatBased> newList = new ArrayList<>();
        for (ChatBased c : chatBasedList) {
            if (c.getName() != null && c.getName().toLowerCase().contains(str)) newList.add(c);
        }

        //add first item create groupChat
        if(visibleGroupChat) {
            ChatBased c = new ChatBased();
            c.setChatId(CREATE_GROUP_CHAT);
            newList.add(0, c);
        }

        adapter.setNewUsers(newList);
    }

    public interface OnSelectUserListener {
        void onSelect(ChatBased chatBased, Dialog dialog);
    }

}
