package mobi.sender.tool;

import android.app.Activity;
import android.content.Intent;

import com.sender.library.ChatFacade;

import org.json.JSONObject;

import mobi.sender.ui.MainActivity;
import mobi.sender.ui.RegConfirmActivity;
import mobi.sender.ui.RegIvrActivity;
import mobi.sender.ui.RegOtpActivity;
import mobi.sender.ui.RegPhoneActivity;

/**
 * Created by Smmarat on 12.08.16.
 */
public class RegRouter {

    public static void route(Activity act, String phone, JSONObject data) {

        String servPhone = "";
        int wait = 0;
        if(data.has("phone")) servPhone = data.optString("phone");
        if(data.has("wait")) wait = data.optInt("wait");

        if (ChatFacade.AUTH_STEP_PHONE.equals(data.optString("step"))) {
            act.startActivity(new Intent(act, RegPhoneActivity.class)
                    .putExtra(RegPhoneActivity.EXTRA_PHONE, phone));
        }
        else if (ChatFacade.AUTH_STEP_CONFIRM.equals(data.optString("step"))) {
            act.startActivity(new Intent(act, RegConfirmActivity.class)
                    .putExtra(RegPhoneActivity.EXTRA_PHONE, phone)
                    .putExtra(RegPhoneActivity.EXTRA_DEVICE, data.optString("devName")));
        }
        else if (ChatFacade.AUTH_STEP_OTP.equals(data.optString("step"))) {
            act.startActivity(new Intent(act, RegOtpActivity.class)
                    .putExtra(RegPhoneActivity.EXTRA_PHONE, servPhone).putExtra(RegOtpActivity.WAIT, wait));
        }
        else if (ChatFacade.AUTH_STEP_LIGHT_IVR.equals(data.optString("step"))) {
            act.startActivity(new Intent(act, RegIvrActivity.class)
                    .putExtra(RegPhoneActivity.EXTRA_PHONE, servPhone));
        }
        else if (ChatFacade.AUTH_STEP_FINISH.equals(data.optString("step"))) {
            act.startActivity(new Intent(act, MainActivity.class));
        }
        act.finish();
    }
}
