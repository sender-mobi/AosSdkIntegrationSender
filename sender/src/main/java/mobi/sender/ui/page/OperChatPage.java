package mobi.sender.ui.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.GetUserInfoReq;
import mobi.sender.event.SetOperOnlineReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.ChatBased;
import mobi.sender.tool.Storage;
import mobi.sender.ui.SearchActivity;
import mobi.sender.ui.adapter.ExpandableListAdapter;

public class OperChatPage extends ChatsPageBased {

    private View view;
    private ExpandableListAdapter adapter;
    private ExpandableListView expListView;
    private SwitchCompat opcSwitch;
    private List<String> listDataHeader = new ArrayList<>();
    private List<String> listIconHeader = new ArrayList<>();
    private HashMap<String, ArrayList<ChatBased>> listDataChild = new HashMap<>();
    private static final String CT = "ct";
    private static final String PHOTO = "photo";
    private static final String NAME = "name";

    public OperChatPage(Context ctx) {
        super(ctx);
        initViews();
        initAdapterAndListeners();
        reload();
    }

    @Override
    protected List<ChatBased> getChats() {
        return null;
    }

    /**
     * Init views.
     */
    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.operator_chat, null);
        expListView = (ExpandableListView) view.findViewById(R.id.opc_expLv);
        opcSwitch = (SwitchCompat) view.findViewById(R.id.opc_switch);
        opcSwitch.setChecked(Storage.getInstance(ctx).getOperOnlineStatus());
    }

    /**
     * Make data for adapter.
     */
    public void reload() {
        Storage storage = Storage.getInstance(ctx);

        for (final String compId : storage.getCompaniesId()) {
            if (storage.isUserExists(compId)) {
                showCompany(compId, storage.getUserIcon(compId), storage.getUserName(compId));
            } else {
                Bus.getInstance().post(new GetUserInfoReq(compId, new SyncEvent.SRespListener() {
                    @Override
                    public void onResponse(JSONObject data) {
                        try {
                            JSONObject object = data.getJSONObject(CT);
                            showCompany(compId, object.optString(PHOTO), object.optString(NAME));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }));
            }
        }
    }

    private void showCompany(final String compId, final String icon, String name) {
        if("".equals(name)){
            name = compId;
        }
        final String finalName = name;
        ((Activity)ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listIconHeader.add(icon);
                listDataHeader.add(finalName);
                listDataChild.put(finalName, Storage.getInstance(ctx).getCompChats(compId));
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Init adapter and listeners. When you click on a group of all other closing
     */
    private void initAdapterAndListeners() {
        opcSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Storage.getInstance(ctx).setOperOnlineStatus(b);
                Bus.getInstance().post(new SetOperOnlineReq(b));
            }
        });
        adapter = new ExpandableListAdapter(ctx, listDataHeader, listDataChild, listIconHeader);
        expListView.setAdapter(adapter);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousItem = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousItem) {
                    expListView.collapseGroup(previousItem);
                }
                previousItem = groupPosition;
            }
        });
    }

    public View getView() {
        return view;
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
                Toast.makeText(ctx, "History", Toast.LENGTH_SHORT).show();
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

        fab1.setImageResource(R.drawable.ic_history);
        fab2.setImageResource(R.drawable.ic_search_sender);

        fab1.show();
        fab2.show();
        fab3.hide();
    }
}
