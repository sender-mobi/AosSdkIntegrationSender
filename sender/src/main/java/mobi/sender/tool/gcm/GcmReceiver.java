package mobi.sender.tool.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import mobi.sender.Bus;
import mobi.sender.Sender;
import mobi.sender.event.SendOnlineReq;
import mobi.sender.model.StateHolder;
import mobi.sender.tool.Tool;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 14.07.14
 * Time: 14:21
 */
public class GcmReceiver extends BroadcastReceiver {

    /**
     * Receiver incoming GCM messages
     */

    @Override
    public void onReceive(final Context context, Intent intent) {
        String message = intent.getExtras().getString("message");
        Tool.log("GCM! " + message);
        if (message == null) {
            Tool.log("message not received");
            return;
        }
        try {
            JSONObject jo = new JSONObject(message);
            if (StateHolder.getInstance(context).isDialogsSynced()) {
                if ("wake_up".equalsIgnoreCase(jo.optString("status"))) {
                    context.startService(new Intent(context, Sender.class));
                }
                if (jo.has("online_key")) {
                    String onlineKey = jo.optString("online_key");
                    Bus.getInstance().post(new SendOnlineReq(onlineKey));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}