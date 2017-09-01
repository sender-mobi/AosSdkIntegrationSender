package mobi.sender.ui.page;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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
import mobi.sender.tool.utils.AttrUtils;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.ui.CardCompanyActivity;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.adapter.DialogRecAdapter;

abstract class ChatsPageBased {
    protected Context ctx;
    private DialogRecAdapter adapter;

    final Animation show_fab;
    final Animation hide_fab;
    final Animation show_fab_1;
    final Animation hide_fab_1;
    final Animation show_fab_2;
    final Animation hide_fab_2;
    private final Animation show_fab_3;
    private final Animation hide_fab_3;

    final float koef1;
    final float koef2;
    final float koef3;
    final float padingRight;
    //Save the FAB's active status: false -> fab = close, true -> fab = open.
    private static boolean fabStatus = false;

    static boolean getFabStatus() {
        return fabStatus;
    }

    static void setFabStatus(boolean fabStatus) {
        ChatsPageBased.fabStatus = fabStatus;
    }

    ChatsPageBased(Context ctx) {
        this.ctx = ctx;

        show_fab = AnimationUtils.loadAnimation(ctx, R.anim.fab_show);
        hide_fab = AnimationUtils.loadAnimation(ctx, R.anim.fab_hide);
        show_fab_1 = AnimationUtils.loadAnimation(ctx, R.anim.fab1_show);
        hide_fab_1 = AnimationUtils.loadAnimation(ctx, R.anim.fab1_hide);
        show_fab_2 = AnimationUtils.loadAnimation(ctx, R.anim.fab2_show);
        hide_fab_2 = AnimationUtils.loadAnimation(ctx, R.anim.fab2_hide);
        show_fab_3 = AnimationUtils.loadAnimation(ctx, R.anim.fab3_show);
        hide_fab_3 = AnimationUtils.loadAnimation(ctx, R.anim.fab3_hide);

        //Get all koef for animation from dimension
        TypedValue outValue = new TypedValue();
        ctx.getResources().getValue(R.dimen.koef1, outValue, true);
        koef1 = outValue.getFloat();
        ctx.getResources().getValue(R.dimen.koef2, outValue, true);
        koef2 = outValue.getFloat();
        ctx.getResources().getValue(R.dimen.koef3, outValue, true);
        koef3 = outValue.getFloat();
        ctx.getResources().getValue(R.dimen.padding_right, outValue, true);
        padingRight = outValue.getFloat();
    }

    protected abstract List<ChatBased> getChats();

    protected abstract String getName();

    public abstract void getFab(FloatingActionButton fab,
                                FloatingActionButton fab1,
                                FloatingActionButton fab2,
                                FloatingActionButton fab3);

    public void reload() {
        if (getChats() != null && getChats().size() != 0 && adapter != null) {
            adapter.setChats(getChats());
        }
    }

    public View getView(final boolean isJustComp) {
        Tool.log("*** getView = "+getChats().size());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout root = new LinearLayout(ctx);
        root.setLayoutParams(params);
        root.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.background_light));
        //make recycler...
        RecyclerView rv = new RecyclerView(ctx);
        adapter = new DialogRecAdapter(ctx, getChats(), isJustComp, new DialogRecAdapter.OnItemClickListener() {
            @Override
            public void onClick(ChatBased c) {
                if (c instanceof User) {
                    User u = (User) c;
                    if (u.isCompany() && u.getCountUnread() == 0) {
                        ctx.startActivity(new Intent(ctx, CardCompanyActivity.class).putExtra(CardCompanyActivity.CHAT_ID, c.getChatId()));
                    } else {
                        ctx.startActivity(new Intent(ctx, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, c.getChatId()));
                    }
                } else {
                    ctx.startActivity(new Intent(ctx, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, c.getChatId()));
                }
            }

            @Override
            public void onLongClick(final ChatBased c, final int adapterPos) {
                Storage storage = Storage.getInstance(ctx);
                final boolean isFav = storage.isFavorite(c.getChatId());
                CharSequence[] items;
                if (Tool.isP2PChat(c.getChatId())) {
                    if(!isJustComp) {
                        items = new CharSequence[]{ctx.getString(isFav ? R.string.dlg_remove_from_favorites : R.string.dlg_add_to_favorites)};
                        DialogUtils.itemsDialog(ctx, R.string.dlg_select_action, items, new DialogUtils.OnChooseListener() {
                            @Override
                            public void onSelect(int position) {
                                switch (position) {
                                    case 0:
                                        Bus.getInstance().post(new SetChatOptionsReq(!isFav, c.getChatId()));
                                        break;
                                }
                            }
                        });
                    }

                } else {
                    items = new CharSequence[]{ctx.getString(isFav ? R.string.dlg_delete_dialog : R.string.dlg_delete_dialog), ctx.getString(isFav ? R.string.dlg_remove_from_favorites : R.string.dlg_add_to_favorites)};

                    DialogUtils.itemsDialog(ctx, R.string.dlg_select_action, items, new DialogUtils.OnChooseListener() {
                        @Override
                        public void onSelect(final int position) {
                            switch (position) {
                                case 0:
                                    if (Tool.isP2PChat(c.getChatId())) {
                                        User u = (User) c;
                                        Bus.getInstance().post(new UpdateCtReq(u.getUserId(), u.getName(), false, new SyncEvent.SRespListener() {
                                            @Override
                                            public void onResponse(JSONObject data) {
                                                adapter.removeAt(adapterPos);
                                                Storage.getInstance(ctx).deleteDialog(c.getChatId());
                                            }

                                            @Override
                                            public void onError(Exception e) {

                                            }
                                        }));
                                    } else {
                                        Bus.getInstance().post(new LeaveChatReq(c.getChatId()));
                                        adapter.removeAt(adapterPos);
                                    }
                                    break;
                                case 1:
                                    Bus.getInstance().post(new SetChatOptionsReq(!isFav, c.getChatId()));
                                    break;
                            }
                        }
                    });
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setAdapter(adapter);
        rv.setLayoutManager(layoutManager);
        //...end
        root.addView(rv, params);

        root.setBackgroundResource(AttrUtils.getColor(ctx, R.attr.attr_background));

        return root;
    }

    void fabClickLogic(final FloatingActionButton fab,
                       final FloatingActionButton fab1,
                       final FloatingActionButton fab2,
                       final FloatingActionButton fab3) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!fabStatus) {
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
                            fab3.startAnimation(show_fab_3);
                            fab3.setClickable(true);
                            fabStatus = true;
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
                            fab3.startAnimation(hide_fab_3);
                            fab3.setClickable(false);
                            fabStatus = false;
                        }
                    }
                });
            }
        });
    }

    void closeFab(final FloatingActionButton fab,
                  final FloatingActionButton fab1,
                  final FloatingActionButton fab2,
                  final FloatingActionButton fab3) {

        fab1.hide();
        fab2.hide();
        fab3.hide();

        if (fabStatus) {
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
            fab3.startAnimation(hide_fab_3);
            fab3.setClickable(false);
            fabStatus = false;
        }
    }

    void setVisibleAllFabs(FloatingActionButton fab1,
                           FloatingActionButton fab2,
                           FloatingActionButton fab3) {
        fab1.show();
        fab2.show();
        fab3.show();

        fab1.setImageResource(R.drawable.ic_write_sender);
        fab2.setImageResource(R.drawable.ic_add_friend);
        fab3.setImageResource(R.drawable.ic_search_sender);
    }
}
