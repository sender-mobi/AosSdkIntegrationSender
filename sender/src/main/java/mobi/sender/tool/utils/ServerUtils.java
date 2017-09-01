package mobi.sender.tool.utils;

import android.app.Activity;
import android.widget.Toast;

import mobi.sender.R;

/**
 * Created by Zver on 26.10.2016.
 */

public class ServerUtils {

    private final static String WAIT = "wait";
    private final static String CANCEL_AUTH_BY_PHONE = "cancel_auth_by_phone";
    private final static String ERROR_SEND_IVR = "error_send_ivr";
    private final static String ERROR_SEND_OTP = "error_count_otp";
    private final static String ERROR_TIMEOUT_OTP = "error_timeout_otp";
    private final static String WRONG_OTP = "wrong_otp";
    private final static String BLOCKED = "blocked";
    private final static String WRONG_PHONE = "wrong_phone";
    private final static String NEED_AUTH_BY_PHONE_FIRST = "need_auth_by_phone_first";
    private final static String TIMEOUT = "timeout";
    private final static String LIMIT = "limit";
    private final static String MANY_REQ = "many_req";


    public static void makeErrorMessage(final Activity act, String error) {
        if (CANCEL_AUTH_BY_PHONE.equals(error)) {
            makeToast(act, act.getString(R.string.tst_you_have_canceled_auth));
        } else if (ERROR_SEND_OTP.equals(error)) {
            makeToast(act, act.getString(R.string.tst_exceed_otp_pass));
        } else if (ERROR_TIMEOUT_OTP.equals(error)) {
            makeToast(act, act.getString(R.string.tst_otp_pass_has_expired));
        } else if (WRONG_OTP.equals(error)) {
            makeToast(act, act.getString(R.string.tst_you_entered_incorrect_sms));
        } else if (WRONG_PHONE.equals(error)) {
            makeToast(act, act.getString(R.string.tst_wrong_number));
        } else if (NEED_AUTH_BY_PHONE_FIRST.equals(error)) {
            makeToast(act, act.getString(R.string.tst_register_on_the_mobile_version));
        } else if (LIMIT.equals(error) || ERROR_SEND_IVR.equals(error)) {
            makeToast(act, act.getString(R.string.tst_exceeded_allowed_number));
        } else if (MANY_REQ.equals(error)){
            makeToast(act, act.getString(R.string.tst_exceeded_allowed_number));
        }
    }

    private static void makeToast(final Activity act, final String mess) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(act, mess, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
