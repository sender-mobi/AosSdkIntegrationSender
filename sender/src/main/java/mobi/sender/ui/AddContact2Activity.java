package mobi.sender.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.GetCtByPhoneReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.event.UpdateCtReq;
import mobi.sender.model.User;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.UiUtils;
import mobi.sender.ui.adapter.AddSeveralContactAdapter;

public class AddContact2Activity extends BaseActivity {

    public static final String PHONE = "phone";
    private CheckBox cbUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact2);
        cbUser = (CheckBox) findViewById(R.id.cb_user);
        UiUtils.initToolbar(this, getString(R.string.adc_add_user), true);
        getDataFromServer();
    }

    @Override
    protected void fromServer(Bus.Event evt) {}

    private void getDataFromServer() {
        String phone = getIntent().getStringExtra(PHONE);
        Bus.getInstance().post(new GetCtByPhoneReq(phone, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(final JSONObject data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<User> list = new ArrayList<>();
                        try {
                            JSONArray ja = data.getJSONArray("cts");

                            if(ja.length() != 0) {
                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject jo = ja.getJSONObject(i);
                                    User user = new User(jo);
                                    if (i != 0) {
                                        list.add(user);
                                    } else {
                                        makeUserItem(user);
                                    }

                                    if (jo.optBoolean("isOwn")) {
                                        cbUser.setVisibility(View.GONE);
                                        Toast.makeText(AddContact2Activity.this, R.string.tst_same_user, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }else{
                                cbUser.setVisibility(View.GONE);
                                findViewById(R.id.tv_no_user).setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        AddSeveralContactAdapter adapter = new AddSeveralContactAdapter(AddContact2Activity.this, list, new AddSeveralContactAdapter.CheckBoxListener() {
                            @Override
                            public void checkClick(User user, boolean status) {
                                updateContact(user, status);
                            }
                        });

                        ((ListView) findViewById(R.id.lv_company)).setAdapter(adapter);

                        findViewById(R.id.tv_add_company).setVisibility(list.size() == 0 ? View.GONE : View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }));
    }

    private void makeUserItem(final User user) {
        ImageView ivAvatar = (ImageView) findViewById(R.id.iv_avatar);
        if(user.getChatPhoto() != null) {
            Tool.loadImage(getApplicationContext(), user.getChatPhoto(), ivAvatar, R.drawable.ic_acc_bg, true);
        }else{
            ivAvatar.setImageResource(R.drawable.ic_acc_bg);
        }
        ((TextView) findViewById(R.id.tv_name)).setText(user.getName());

        //user checkbox logic
        cbUser.setVisibility(View.VISIBLE);
        cbUser.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        updateContact(user, isChecked);
                    }
                }
        );
    }

    /**
     * Make menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done_menu, menu);
        return true;
    }

    /**
     * Done press logic.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (getStor().isEmptyAuthToken()) {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
        return super.onOptionsItemSelected(menuItem);
    }

    private void updateContact(User user, boolean status) {
        Bus.getInstance().post(new UpdateCtReq(user.getUserId(), user.getName(), status, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {

            }

            @Override
            public void onError(Exception e) {

            }
        }));
    }
}
