package mobi.sender.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sender.library.ChatFacade;

import org.json.JSONObject;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.AuthEvent;
import mobi.sender.event.AuthReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.tool.RegRouter;

public class RegIvrActivity extends BaseActivity {

    private String phone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_ivr);
        phone = getIntent().getStringExtra(RegPhoneActivity.EXTRA_PHONE);
        if (phone == null) finish();
        ((TextView) findViewById(R.id.reg_ivr_phone)).setText(phone);
    }

    public void changePhone(View view) {
        Bus.getInstance().post(new AuthReq(ChatFacade.AUTH_ACTION_BREAK, null, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                RegRouter.route(RegIvrActivity.this, phone, data);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    protected void fromServer(Bus.Event evt) {
        if (evt instanceof AuthEvent) {
            if (ChatFacade.AUTH_STEP_FINISH.equals(((AuthEvent) evt).getModel().optString("step"))) {
                startActivity(new Intent(RegIvrActivity.this, MainActivity.class));
                finish();
            }
        }
    }
}
