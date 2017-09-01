package mobi.sender.tool;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.annotation.RawRes;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import mobi.sender.R;
import mobi.sender.Sender;
import mobi.sender.model.msg.MsgBased;
import mobi.sender.model.msg.TextMessage;
import mobi.sender.tool.utils.AttrUtils;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.MainActivity;

/**
 * Created by vp
 * * on 24.12.14.
 */
public class Notificator {

    private static int NOTIF_ID = 543435012;
    private static final long minBeepInterval = 3000;
    private static Notificator instance;
    private Context ctx;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<N>> notifications = new ConcurrentHashMap<>();
    private boolean running = false;
    private int counter = 0;
    private TelephonyManager tm;
    private long lastBeepTime = 0;

    private Notificator(Context ctx) {
        this.ctx = ctx;
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public static Notificator getInstance(Context ctx) {
        if (instance == null) {
            instance = new Notificator(ctx);
        }
        return instance;
    }

    private void beep(boolean fin) {
        if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE
                && System.currentTimeMillis() - lastBeepTime > minBeepInterval) {
            lastBeepTime = System.currentTimeMillis();
            playSound(fin ? R.raw.kassa2 : R.raw.kap2);
        }
        if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(new long[]{400, 200, 400, 200, 400}, -1);
        }
    }

    private float getVolume() {
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        return (float) audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
    }

    public void addNotification(final String chatId, final String from, final String text, final boolean fin, String packetId) {
        Tool.log("new notification: chat: " + chatId + " text: " + text);  //chat: user+i16113903849 text: Единая система авторизации
        Storage storage = Storage.getInstance(ctx);
        if (!storage.isShowNotifications()) {
            Tool.log("Global notification disabled");
            return;
        }
        if (storage.isMute(chatId) || storage.isBlock(chatId)) {
            Tool.log("Chat: " + chatId + " notification skipped");
            return;
        }
        counter++;
        CopyOnWriteArrayList<N> nots;
        if (notifications.containsKey(chatId)) {
            nots = notifications.get(chatId);
        } else {
            nots = new CopyOnWriteArrayList<>();
        }

        MsgBased m = storage.getMessage(chatId, packetId);
        String textN = MsgBased.getTextFromModel(ctx, m.getClassName(), m.getModel(), storage.isFormMessage(chatId, packetId));

        for (N n : nots) {
            if (n.getPacketId().equals(packetId)) {
                int index = notifications.get(chatId).indexOf(n);
                n.setText(textN);

                notifications.get(chatId).set(index, n);
                rebuildNotification();
                if (!running) new Thread(new Checker()).start();
                beep(fin);
                return;
            }
        }

        nots.add(new N(textN, from, packetId));
        notifications.put(chatId, nots);
        rebuildNotification();
        if (!running) new Thread(new Checker()).start();
        beep(fin);
    }

    private void playSound(@RawRes int soundRes) {
        if (!Storage.getInstance(ctx).isPlaySoundNotifications()) {
            Tool.log("Global sound disabled");
            return;
        }
        MediaPlayer player = MediaPlayer.create(ctx, soundRes);
        player.setVolume(getVolume(), getVolume());
        player.start();
    }

    public void removeNotifications(String chatId) {
        Tool.log("try remove all notifications of chat " + chatId);
        List<N> notificationList = notifications.get(chatId);
        if (notificationList != null) {
            counter = counter - notificationList.size();
            notifications.remove(chatId);
            rebuildNotification();
        }
    }

    public void removeNotifications() {
        Tool.log("try remove all notifications");
        counter = 0;
        notifications.clear();
        rebuildNotification();
    }

    private void rebuildNotification() {
        if (notifications.size() == 0) {
            NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancel(NOTIF_ID);
            Tool.log("notification list is empty, all notifications hided");
        } else if (notifications.size() > 1) {
            Tool.log("notification list contains items from different chats");
            showNotification(/*ctx.getString(R.string.)*/
                    " "
                            + counter
                            + " "
                            + ctx.getString(R.string.ntf_new_messages_in_gcm)
                            + " "
                            + notifications.size()
                            + " "
                            + ctx.getString(R.string.ntf_chats_gcm), null);
        } else {
            String chatId = notifications.keys().nextElement();
            Tool.log("notifications only from chat " + chatId);
            List<N> nots = notifications.get(chatId);
            StringBuilder sb = new StringBuilder();
            Storage storage = Storage.getInstance(ctx);
            for (N n : nots) {
                sb.append(storage.getUserName(n.getFromId())).append(": ").append(n.getText()).append("\n");
            }
            String text = sb.substring(0, sb.lastIndexOf("\n"));
            showNotification(text, chatId);
        }
    }

    private void showNotification(final String text, final String chatId) {
        Tool.log("begin show notification... text: " + text);
        Intent i;
        String title;
        if (chatId != null) {
            i = new Intent(ctx, ChatActivity.class);
            i.putExtra(ChatActivity.EXTRA_CHAT_ID, chatId);
            title = Storage.getInstance(ctx).getChatName(chatId);
        } else {
            i = new Intent(ctx, MainActivity.class);
            title = ctx.getResources().getString(R.string.app_name);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, chatId != null ? chatId.hashCode() : new Random().nextInt(1000), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
//                Uri alarmSound = Uri.parse("android.resource://" + ctx.getPackageName() + "/" + R.raw.kap2);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(Storage.getInstance(ctx).isEmptyAuthToken() ?
                                R.drawable.ic_menu_dialog : R.drawable.ic_launcher_white)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(contentIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
//                                .setSound(preferences.getBoolean(App.PREF_SOUND_B, true) ? alarmSound : null);
        android.app.Notification n = mBuilder.build();
        n.flags = android.app.Notification.FLAG_ONLY_ALERT_ONCE | android.app.Notification.FLAG_AUTO_CANCEL;
        n.when = System.currentTimeMillis();
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        nMgr.notify(NOTIF_ID, n);
        Tool.log("end show notification... text: " + text + " title: " + title);

    }

    private class Checker implements Runnable {

        @Override
        public void run() {
            Thread.currentThread().setName("notification checker");
            running = true;
            Tool.log("notification checker started");
            try {
                while (notifications.size() > 0) {
                    for (String chatId : notifications.keySet()) {
                        CopyOnWriteArrayList<N> nots = new CopyOnWriteArrayList<>();
                        for (N n : notifications.get(chatId)) {
                            if (n.isExpired()) {
                                Tool.log("notification " + n.getText() + " is expired");
                                counter--;
                                continue;
                            }
                            nots.add(n);
                        }
                        if (nots.size() == 0) {
                            notifications.remove(chatId);
                            Tool.log("all notification in chat " + chatId + " are expired");
                        } else {
                            notifications.put(chatId, nots);
                        }
                    }
                    if (notifications.size() > 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Tool.log("notification checker stopped");
            NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancel(NOTIF_ID);
            Tool.log("all notifications hided");
            running = false;
        }
    }

    private class N {

        private String text;
        private String fromId;
        private long created;
        public static final long EXP_TIME = 10 * 60 * 60 * 1000;
        private String packetId;


        public N(String text, String fromId, String packetId) {
            this.text = text;
            this.fromId = fromId;
            this.created = System.currentTimeMillis();
            this.packetId = packetId;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - created > EXP_TIME;
        }

        public String getFromId() {
            return fromId;
        }

        public String getText() {
            return text;
        }

        public long getCreated() {
            return created;
        }

        public String getPacketId() {
            return packetId;
        }

        public void setPacketId(String packetId) {
            this.packetId = packetId;
        }
    }
}
