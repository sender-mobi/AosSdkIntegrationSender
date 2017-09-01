package mobi.sender.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.SearchReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.ChatBased;
import mobi.sender.model.User;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.KeyboardUtils;
import mobi.sender.tool.utils.UiUtils;
import mobi.sender.ui.adapter.SearchAdapter;
import mobi.sender.ui.page.CompChatPage;
import mobi.sender.ui.page.FavChatPage;
import mobi.sender.ui.page.GroupChatPage;
import mobi.sender.ui.page.OperChatPage;
import mobi.sender.ui.page.P2PChatPage;

public class SearchActivity extends BaseActivity {

    private SearchAdapter adapter;
    private volatile boolean flag = true;
    private ProgressBar pb;
    private List<ChatBased> basedList = new ArrayList<>();
    private String lastSearch;
    private String page;
    private EditText etSearch;
    public final static String PAGE_NAME = "page_name";
    public final static String CREATE_GROUP_CHAT = "create_group_chat";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        page = getIntent().getStringExtra(PAGE_NAME);
        etSearch = (EditText) findViewById(R.id.et_search);

        UiUtils.initToolbar(this, getString(R.string.adc_add_user), true);

        pb = (ProgressBar) findViewById(R.id.pb_progress);
        pb.getProgressDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        //init ListView, adapter and make default data
        adapter = new SearchAdapter(this, basedList);
        ((ListView) findViewById(R.id.lv_company)).setAdapter(adapter);

        if (page.equals(FavChatPage.class.getName())) {
            basedList = getStor().getFavoriteChats();
        } else if (page.equals(P2PChatPage.class.getName())) {
            basedList = getStor().getUsers(false);
        } else if (page.equals(GroupChatPage.class.getName())) {
            basedList = getStor().getDialogs();
        } else if (page.equals(CompChatPage.class.getName()) || page.equals(OperChatPage.class.getName())) {
            basedList = getStor().getComps();
        }

        adapter.setLocalSize(basedList.size());
        adapter.setUsers(basedList);

        //make new data when typing
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, final int count) {
                makeSearch(s.toString(), count);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //close keyboard when click "Search" button
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.visibleKeyboard(false, etSearch, SearchActivity.this);
                    return true;
                }
                return false;
            }
        });
    }

    private void makeSearch(String s, final int count){
        final List<ChatBased> newList = filter(s);

        if ((page.equals(CompChatPage.class.getName()) || page.equals(OperChatPage.class.getName()))) {
            if (count > 2 || newList.size() == 0) {
                if (flag) {
                    flag = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.VISIBLE);
                        }
                    });

                    Bus.getInstance().post(new SearchReq(s, new SyncEvent.SRespListener() {
                        @Override
                        public void onResponse(JSONObject data) {
                            String t = "";
                            try {
                                t = data.getString("t");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if(t.equals(etSearch.getText().toString())) {
                                JSONArray ja = data.optJSONArray("list");
                                List<User> list = new ArrayList<>();
                                for (int i = 0; i < ja.length(); i++) {
                                    try {
                                        JSONObject jo = ja.getJSONObject(i);
                                        Tool.log("*** jo = "+jo);
                                        User user = new User(jo);
                                        list.add(user);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (lastSearch.length() == etSearch.getText().toString().length()) {
                                    newList.addAll(list);
                                    adapter.setUsers(newList);
                                }
                            }

                            flag = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pb.setVisibility(View.GONE);
                                }
                            });

                            //make search
                            if(!lastSearch.equals(etSearch.getText().toString())){
                                makeSearch(etSearch.getText().toString(), etSearch.getText().toString().length());
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            flag = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pb.setVisibility(View.GONE);
                                }
                            });
                        }
                    }));
                    lastSearch = s;
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            flag = true;
                            pb.setVisibility(View.GONE);
                        }
                    }, 5000);
                }
            }
        }
    }

    private List<ChatBased> filter(String str) {
        List<ChatBased> newList = new ArrayList<>();
        if ("".equals(str)) {
            newList = basedList;
        } else {
            for (ChatBased c : basedList) {
                if (c.getName().toLowerCase().contains(str)) {
                    newList.add(c);
                }
            }
        }

        adapter.setUsers(newList);
        adapter.setLocalSize(newList.size());
        return newList;
    }

    @Override
    protected void fromServer(Bus.Event evt) {
    }

    /**
     * Back pressed logic
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        finish();
        return super.onOptionsItemSelected(menuItem);
    }
}
