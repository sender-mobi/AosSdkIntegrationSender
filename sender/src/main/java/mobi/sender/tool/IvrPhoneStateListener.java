package mobi.sender.tool;

import android.content.Context;
import android.telephony.PhoneStateListener;

import com.sender.library.ChatFacade;

import org.json.JSONObject;

import mobi.sender.Bus;
import mobi.sender.event.AuthReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.StateHolder;

/**
 * Created by Smmarat on 05.09.16.
 */
public class IvrPhoneStateListener extends PhoneStateListener {
    private String prefix;
    private Context ctx;
    public static final String DEFAULT_IVR_PHONE_PREFIX = "380922625";

    public IvrPhoneStateListener(Context ctx, String prefix) {
        this.ctx = ctx;
        if (prefix == null || prefix.isEmpty()) {
            Tool.log("ivr prefix is null");
            prefix = DEFAULT_IVR_PHONE_PREFIX;
        }
        this.prefix = prefix;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        if (incomingNumber == null) return;
        incomingNumber = incomingNumber.replace("+", "").replaceAll(" ", "");
        String prefixWithoutCountryCode = prefix.substring(3, prefix.length());
        Tool.log("Phone call, state: " + state + ", number: " + incomingNumber + ", prefix: " + prefix + ",\n " + "without country code: " + prefixWithoutCountryCode);
        if (state == 1 && (incomingNumber.startsWith(prefix) || incomingNumber.contains(prefixWithoutCountryCode)) && !StateHolder.getInstance(ctx).isRegistered()) {
            Tool.disconnectIncomingCall();

            while (!Tool.isOnline(ctx)) try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Bus.getInstance().post(new AuthReq(ChatFacade.AUTH_ACTION_LIGHT_IVR, incomingNumber, new SyncEvent.SRespListener() {
                @Override
                public void onResponse(JSONObject data) {
                    Tool.log(data.toString());
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            }));
        }
    }
}
