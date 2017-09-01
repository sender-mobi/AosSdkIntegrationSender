package mobi.sender.ui.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import java.util.List;

import mobi.sender.model.ChatBased;
import mobi.sender.tool.Storage;
import mobi.sender.ui.AddContactActivity;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.CreateGroupChatActivity;
import mobi.sender.ui.SearchActivity;
import mobi.sender.ui.window.AddOneUserWindow;

/**
 * Created by vp on 19.07.16.
 */
public class P2PChatPage extends ChatsPageBased {

    public P2PChatPage(Context ctx) {
        super(ctx);
    }

    @Override
    protected List<ChatBased> getChats() {
        return Storage.getInstance(ctx).getUsers(false);
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void getFab(final FloatingActionButton fab,
                       final FloatingActionButton fab1,
                       final FloatingActionButton fab2,
                       final FloatingActionButton fab3) {

        closeFab(fab, fab1, fab2, fab3);
        fabClickLogic(fab, fab1, fab2, fab3);
        setVisibleAllFabs(fab1, fab2, fab3);

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddOneUserWindow((Activity) ctx, getChats(), true, new AddOneUserWindow.OnSelectUserListener() {
                    @Override
                    public void onSelect(ChatBased chatBased, android.app.Dialog dialog) {
                        if(AddOneUserWindow.CREATE_GROUP_CHAT.equals(chatBased.getChatId())){
                            ctx.startActivity(new Intent(ctx, CreateGroupChatActivity.class));
                        }else {
                            ctx.startActivity(new Intent(ctx, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, chatBased.getChatId()));
                        }
                    }
                }).show();
            }
        });

        //init listeners
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.startActivity(new Intent(ctx, AddContactActivity.class));
            }
        });

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ctx, SearchActivity.class);
                i.putExtra(SearchActivity.PAGE_NAME, getName());
                ctx.startActivity(i);
            }
        });
    }

}
