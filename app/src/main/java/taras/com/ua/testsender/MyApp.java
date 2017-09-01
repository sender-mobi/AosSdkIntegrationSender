package taras.com.ua.testsender;

import android.content.Intent;

import mobi.sender.App;
import mobi.sender.Bus;
import mobi.sender.event.Code3Event;
import mobi.sender.event.P24ChangeBtcEvent;
import mobi.sender.event.P24enableFullVerReq;
import mobi.sender.event.P24onBitcoinClickEvent;
import mobi.sender.event.P24onRegEvent;
import mobi.sender.event.P24openEvent;
import mobi.sender.event.SyncDlgReq;
import mobi.sender.event.UndefinedEvent;
import mobi.sender.model.StateHolder;
import mobi.sender.tool.Tool;
import mobi.sender.ui.SyncDlgActivity;

/**
 * Created by Zver on 23.03.2017.
 */

public class MyApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();
        registerEvents();
    }

    private void registerEvents() {
        Bus.getInstance().register(this, Code3Event.class.getSimpleName());
        Bus.getInstance().register(this, UndefinedEvent.class.getSimpleName());
        Bus.getInstance().register(this, P24onRegEvent.class.getSimpleName());
        Bus.getInstance().register(this, P24ChangeBtcEvent.class.getSimpleName());
        Bus.getInstance().register(this, P24onBitcoinClickEvent.class.getSimpleName());
        Bus.getInstance().register(this, P24openEvent.class.getSimpleName());
        Bus.getInstance().register(this, P24enableFullVerReq.class.getSimpleName());
    }

    @Override
    public void onEvent(Bus.Event evt) {
        if (evt instanceof Code3Event) {

        } else if (evt instanceof UndefinedEvent) {
        } else if (evt instanceof P24onRegEvent || evt instanceof P24ChangeBtcEvent) {
            StateHolder.getInstance(this).setWalletSynced();
            Intent i = new Intent(this, SyncDlgActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else if (evt instanceof P24openEvent) {
        } else if (evt instanceof P24enableFullVerReq) {
        } else if (evt instanceof P24onBitcoinClickEvent){
        }
    }

}
