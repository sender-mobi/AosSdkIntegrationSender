package mobi.sender.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONObject;

import mobi.sender.Bus;
import mobi.sender.Sender;
import mobi.sender.event.GetMyInfoReq;
import mobi.sender.event.SyncEvent;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 21.09.14
 * Time: 13:47
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected() && Sender.isDisplayedApplication(context)) {
            Tool.log("== Network connected!");
            Bus.getInstance().post(new GetMyInfoReq(new SyncEvent.SRespListener() {
                @Override
                public void onResponse(JSONObject data) {}

                @Override
                public void onError(Exception e) {}
            }));
        }
    }
}
