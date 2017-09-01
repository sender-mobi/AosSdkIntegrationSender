package mobi.sender.ui;

import android.content.Intent;
import android.os.Bundle;

import mobi.sender.Bus;
import mobi.sender.Sender;
import mobi.sender.SenderHelper;
import mobi.sender.event.P24openEvent;
import mobi.sender.event.StatusInternetEvent;
import mobi.sender.model.StateHolder;
import mobi.sender.tool.Tool;

public class MainActivity extends BaseActivity {

    private SenderHelper sh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getSh().create());
    }

    @Override
    protected String getCurrChatId() {
        return Sender.ALL_CHAT_ID;
    }

    @Override
    public void onBackPressed() {
        finish();
        if (!getStor().isEmptyAuthToken() && isTaskRoot()) {
            Bus.getInstance().post(new P24openEvent());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        StateHolder holder = StateHolder.getInstance(this);
        Tool.log("resume, state is:" + holder.getCurrState());
        if (!holder.isWalletSynced()) {
            startActivity(new Intent(this, SyncBtcActivity.class));
        } else if (!holder.isDialogsSynced()) {
            startActivity(new Intent(this, SyncDlgActivity.class));
        } else {
            getSh().update();
        }
    }

    @Override
    protected void fromServer(Bus.Event evt) {
    }

    //SenderHelper logic
    public SenderHelper getSh() {
        if (sh == null) {
            if ("".equals(getStor().getAuthToken())) {
                return sh = new SenderHelper(this, true, new SenderHelper.OnEventListener() {
                    @Override
                    public void shOnEvent(Bus.Event evt) {
                        if (evt instanceof StatusInternetEvent) {
                            StatusInternetEvent event = ((StatusInternetEvent) evt);
                            makeSnakBar(event.isVisible());
                        } else {
                            fromServer(evt);
                        }
                    }
                }, StartActivity.devId, StartActivity.devKey, null, null);
            } else {
                return sh = new SenderHelper(this, false, new SenderHelper.OnEventListener() {
                    @Override
                    public void shOnEvent(Bus.Event evt) {
                        if (evt instanceof StatusInternetEvent) {
                            StatusInternetEvent event = ((StatusInternetEvent) evt);
                            makeSnakBar(event.isVisible());
                        } else {
                            fromServer(evt);
                        }
                    }
                }, getStor().getDevId(), getStor().getDevKey(), getStor().getAuthToken(), getStor().getCompanyId());
            }
        }
        return sh;
    }

    @Override
    protected void registerBus(Bus.Subscriber subscriber) {
        super.registerBus(getSh());
    }

    @Override
    protected void unregisterBus(Bus.Subscriber subscriber) {
        super.unregisterBus(getSh());
    }
}
