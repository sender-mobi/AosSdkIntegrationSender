package mobi.sender.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.R;
import mobi.sender.model.ChatBased;
import mobi.sender.model.User;
import mobi.sender.tool.Storage;
import mobi.sender.ui.CardCompanyActivity;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.adapter.DialogRecAdapter;

/**
 * Created by mw on 17.03.17.
 */

public class SharedFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    private String sharedText = "";
    private String sharedImage = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shared, container, false);
        int tabPos = getArguments().getInt(ARG_OBJECT);
        sharedText = getArguments().getString(ChatActivity.EXTRA_TEXT);
        sharedImage = getArguments().getString(ChatActivity.EXTRA_IMAGE);

        List<ChatBased> chats = new ArrayList<>();
        if (tabPos == 0) {
            chats = Storage.getInstance(getActivity()).getUsers(false);
        } else if (tabPos == 1) {
            chats = Storage.getInstance(getActivity()).getGroupChats();
        }

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv_shared);
        boolean isJustComp = (Storage.getInstance(getActivity()).isEmptyAuthToken() && Storage.getInstance(getActivity()).isFullVer());
        DialogRecAdapter adapter = new DialogRecAdapter(getActivity(), chats, isJustComp, new DialogRecAdapter.OnItemClickListener() {
            @Override
            public void onClick(ChatBased c) {
                if (c instanceof User) {
                    User u = (User) c;
                    if (u.isCompany() && u.getCountUnread() == 0) {
                        getActivity().startActivity(new Intent(getActivity(), CardCompanyActivity.class).putExtra(CardCompanyActivity.CHAT_ID, c.getChatId()));
                    } else {
                        Intent i = new Intent(getActivity(), ChatActivity.class);
                        i.putExtra(ChatActivity.EXTRA_CHAT_ID, c.getChatId());
                        if (!"".equals(sharedText))
                            i.putExtra(ChatActivity.EXTRA_TEXT, sharedText);
                        if (!"".equals(sharedImage))
                            i.putExtra(ChatActivity.EXTRA_IMAGE, sharedImage);
                        getActivity().startActivity(i);
                    }
                } else {
                    getActivity().startActivity(new Intent(getActivity(), ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, c.getChatId()));
                }

                //delete shared data from adapter
                sharedText = "";
                sharedImage = "";
            }

            @Override
            public void onLongClick(ChatBased c, int pos) {
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setAdapter(adapter);
        rv.setLayoutManager(layoutManager);
        return rootView;
    }
}
