package mobi.sender.tool.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import mobi.sender.tool.Tool;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 27.03.14
 * Time: 15:11
 */
public class GCMHelper {

    private static final String PROP_REG_ID = "gcm_token";
    private static final String PROP_GCM_REG_TIME = "prop_reg_id";
    private static final String GCM_SENDER_ID = "341256065955";
    private static final String PROP_APP_VERSION = "prop_app_version";
    private Context ctx;
    private SharedPreferences preferences;

    public GCMHelper(Context ctx) {
        this.ctx = ctx;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void getGcmId(final GcmRegCallback callback) {
        try {
            final String regId = preferences.getString(PROP_REG_ID, "");
            long lastCheckTime = preferences.getLong(PROP_GCM_REG_TIME, 0L);
            if (regId.isEmpty() || isVerChanged(ctx) || System.currentTimeMillis() - lastCheckTime > 5 * 60 * 1000) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String nRegId = GoogleCloudMessaging.getInstance(ctx).register(GCM_SENDER_ID);
                            Tool.log("G.C.M Device registered, registration ID=" + nRegId);
                            regCurrVersion(ctx);
                            Tool.log("G.C.M old: " + regId + " new: " + nRegId);
                            if (!regId.equalsIgnoreCase(nRegId)) {
                                boolean sent = callback.onRegSuccess(nRegId);
                                if (sent) {
                                    preferences.edit().putString(PROP_REG_ID, nRegId).putLong(PROP_GCM_REG_TIME, System.currentTimeMillis()).apply();
                                    Tool.log("G.C.M sent");
                                } else {
                                    Tool.log("G.C.M not sent");
                                }
                            }
                        } catch (IOException ex) {
                            callback.onError(ex);
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            preferences.edit().remove(PROP_REG_ID).remove(PROP_GCM_REG_TIME).apply();
            getGcmId(callback);
        }
    }

    private boolean isVerChanged(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        int registeredVersion = preferences.getInt(PROP_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Tool.getAppVersion(ctx);
        return registeredVersion != currentVersion;
    }

    private void regCurrVersion(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        preferences.edit().putInt(PROP_APP_VERSION, Tool.getAppVersion(ctx)).apply();
    }

//    private boolean checkPlayServices() {
//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                GooglePlayServicesUtil.getErrorDialog(resultCode, ctx, PLAY_SERVICES_RESOLUTION_REQUEST).show();
//            } else {
//                App.log("This device is not supported.");
//            }
//            return false;
//        }
//        return true;
//    }

    public interface GcmRegCallback {
        boolean onRegSuccess(String regId);

        void onError(Exception e);
    }

}
