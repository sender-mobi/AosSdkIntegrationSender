package mobi.sender.ui.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

import mobi.sender.R;
import mobi.sender.model.ChatBased;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.SearchActivity;
import mobi.sender.ui.window.AddOneUserWindow;

/**
 * Created by vp on 19.07.16.
 */
public class CompChatPage extends ChatsPageBased {

    public CompChatPage(Context ctx) {
        super(ctx);
    }

    @Override
    protected List<ChatBased> getChats() {
        return Storage.getInstance(ctx).getComps();
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!getFabStatus()) {
                            //Display FAB menu
                            fab.startAnimation(show_fab);

                            //Floating Action Button 1
                            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) fab1.getLayoutParams();
                            layoutParams1.rightMargin += (int) (fab1.getWidth() * padingRight);
                            layoutParams1.bottomMargin += (int) (fab1.getHeight() * koef1);
                            fab1.setLayoutParams(layoutParams1);
                            fab1.startAnimation(show_fab_1);
                            fab1.setClickable(true);

                            //Floating Action Button 2
                            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fab2.getLayoutParams();
                            layoutParams2.rightMargin += (int) (fab2.getWidth() * padingRight);
                            layoutParams2.bottomMargin += (int) (fab2.getHeight() * koef2);
                            fab2.setLayoutParams(layoutParams2);
                            fab2.startAnimation(show_fab_2);
                            fab2.setClickable(true);

                            //Floating Action Button 3
                            FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) fab3.getLayoutParams();
                            layoutParams3.rightMargin += (int) (fab3.getWidth() * padingRight);
                            layoutParams3.bottomMargin += (int) (fab3.getHeight() * koef3);
                            fab3.setLayoutParams(layoutParams3);
//                            fab3.startAnimation(show_fab_3);
                            fab3.setClickable(true);
                            fab3.hide();
                            setFabStatus(true);
                        } else {
                            //Close FAB menu
                            //Floating Action Button
                            fab.startAnimation(hide_fab);

                            //Floating Action Button 1
                            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) fab1.getLayoutParams();
                            layoutParams1.rightMargin -= (int) (fab1.getWidth() * padingRight);
                            layoutParams1.bottomMargin -= (int) (fab1.getHeight() * koef1);
                            fab1.setLayoutParams(layoutParams1);
                            fab1.startAnimation(hide_fab_1);
                            fab1.setClickable(false);

                            //Floating Action Button 2
                            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fab2.getLayoutParams();
                            layoutParams2.rightMargin -= (int) (fab2.getWidth() * padingRight);
                            layoutParams2.bottomMargin -= (int) (fab2.getHeight() * koef2);
                            fab2.setLayoutParams(layoutParams2);
                            fab2.startAnimation(hide_fab_2);
                            fab2.setClickable(false);

                            //Floating Action Button 3
                            FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) fab3.getLayoutParams();
                            layoutParams3.rightMargin -= (int) (fab3.getWidth() * padingRight);
                            layoutParams3.bottomMargin -= (int) (fab3.getHeight() * koef3);
                            fab3.setLayoutParams(layoutParams3);
//                            fab3.startAnimation(hide_fab_3);
                            fab3.setClickable(false);
                            fab3.hide();
                            setFabStatus(false);
                        }
                    }
                });
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddOneUserWindow((Activity) ctx, Storage.getInstance(ctx).getComps(), false, new AddOneUserWindow.OnSelectUserListener() {
                    @Override
                    public void onSelect(ChatBased chatBased, android.app.Dialog dialog) {
                        ctx.startActivity(new Intent(ctx, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, chatBased.getChatId()));
                    }
                }).show();
            }
        });

        //init listeners
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ctx, SearchActivity.class);
                i.putExtra(SearchActivity.PAGE_NAME, getName());
                ctx.startActivity(i);
            }
        });

        fab1.setImageResource(R.drawable.ic_write_sender);
        fab2.setImageResource(R.drawable.ic_search_sender);

        fab1.show();
        fab2.show();
        fab3.hide();
    }

}
