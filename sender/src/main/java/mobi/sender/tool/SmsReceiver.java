package mobi.sender.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by Zver on 13.12.2016.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");

        if (pdus != null) {
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);

//                String sender = smsMessage.getDisplayOriginatingAddress();
                //You must check here if the sender is your provider and not another one with same text.

                String messageBody = smsMessage.getMessageBody();

                if (messageBody.contains("sender") && messageBody.replaceAll("\\D+","").length() == 4 && "".equals(Storage.getInstance(context).getMyUserId())) {
                    //Pass on the text to our listener.
                    if (mListener != null) mListener.messageReceived(messageBody.replaceAll("\\D+",""));
                }
            }
        }

    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }

    public interface SmsListener {
        void messageReceived(String messageText);
    }

}
