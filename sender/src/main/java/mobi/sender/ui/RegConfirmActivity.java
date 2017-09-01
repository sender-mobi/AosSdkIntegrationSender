package mobi.sender.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sender.library.ChatFacade;

import org.json.JSONException;
import org.json.JSONObject;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.AuthEvent;
import mobi.sender.event.AuthReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.tool.RegRouter;
import mobi.sender.tool.utils.ServerUtils;
import mobi.sender.tool.utils.UiUtils;

public class RegConfirmActivity extends BaseActivity {

    private String phone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_confirm);
        phone = getIntent().getStringExtra(RegPhoneActivity.EXTRA_PHONE);
        String device = getIntent().getStringExtra(RegPhoneActivity.EXTRA_DEVICE);
        if (device != null) ((TextView)findViewById(R.id.reg_confirm_device)).setText(device);
        UiUtils.initToolbar(this, getString(R.string.tlb_confirmation), false);
    }

    public void cancelConfirm(View view) {
        findViewById(R.id.reg_progress).setVisibility(View.VISIBLE);
        Bus.getInstance().post(new AuthReq(ChatFacade.AUTH_ACTION_BREAK, null, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
                    }
                });
                if(data.has("error")){
                    try {
                        ServerUtils.makeErrorMessage(RegConfirmActivity.this, data.getString("error"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                RegRouter.route(RegConfirmActivity.this, phone, data);
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
                    }
                });
                e.printStackTrace();
            }
        }));
    }

    @Override
    protected void fromServer(Bus.Event evt) {
        if (evt instanceof AuthEvent) {
            RegRouter.route(RegConfirmActivity.this, phone, ((AuthEvent) evt).getModel());
        }
    }
}
