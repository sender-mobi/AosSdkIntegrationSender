package mobi.sender.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import org.json.JSONObject;

import java.util.Map;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.CompFormUpdatedEvent;
import mobi.sender.event.GetCompFormReq;
import mobi.sender.tool.ActionExecutor;
import mobi.sender.tool.Tool;
import mobi.sender.tool.fml.FMLRenderer;
import mobi.sender.tool.utils.UiUtils;
import mobi.sender.ui.adapter.MsgRecAdapter;

public class CardCompanyActivity extends BaseActivity implements MsgRecAdapter.GetActivity {

    public final static String CHAT_ID = "chat_id";
    private String chatId = "";

    @Override
    protected void fromServer(Bus.Event evt) {
        if (evt instanceof CompFormUpdatedEvent) {
            makeUi();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_company);
        UiUtils.initToolbar(this, getString(R.string.chi_company_card), true);

        //get intent from extra
        if (getIntent().hasExtra(CHAT_ID)) {
            chatId = getIntent().getStringExtra(CHAT_ID);
        }

        makeUi();
        Bus.getInstance().post(new GetCompFormReq(chatId));
    }

    private void makeUi() {
        //get data from storage
        final JSONObject jo = getStor().getCompForm(chatId);

        final ActionExecutor actionExecutor = new ActionExecutor(this, chatId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    FrameLayout root = (FrameLayout) findViewById(R.id.fl_form);
                    root.removeAllViews();
                    root.addView(new FMLRenderer(chatId, CardCompanyActivity.this).makeView(jo, new FMLRenderer.SendListener() {
                        @Override
                        public void doSend(JSONObject data) {
                        }

                        @Override
                        public void doAction(String oper, final JSONObject data, Map<String, Object> params, final FMLRenderer.ActionProcessListener apl) {
                            actionExecutor.setOnActionListener(new ActionExecutor.OnActionFinished() {
                                @Override
                                public void onActionFinished(boolean disableForm) {
                                    apl.onProcess(disableForm);
                                }

                                @Override
                                public void refreshForm() {
                                    // disabled for comp forms
                                }
                            });
                            actionExecutor.exec(data, oper, params);
                        }
                    }));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == R.id.action_chat) {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra(ChatActivity.EXTRA_CHAT_ID, chatId);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

        }
        finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Make menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.card_menu, menu);
        return true;
    }

    @Override
    public Activity getAct() {
        return this;
    }
}
