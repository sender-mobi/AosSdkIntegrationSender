package mobi.sender.tool.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

import mobi.sender.Bus;
import mobi.sender.event.RefreshTokenEvent;

/**
 * Created by Zver on 26.01.2017.
 */

public class GCMTokenRefreshListenerService extends InstanceIDListenerService {

    //If the token is changed registering the device again
    @Override
    public void onTokenRefresh() {
        Bus.getInstance().post(new RefreshTokenEvent());
    }
}