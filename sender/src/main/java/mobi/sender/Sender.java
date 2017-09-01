package mobi.sender;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.webkit.MimeTypeMap;

import com.sender.library.ChatFacade;
import com.sender.library.Log;
import com.sender.library.SenderRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mobi.sender.event.Add2ChatReq;
import mobi.sender.event.AlertReq;
import mobi.sender.event.AuthEvent;
import mobi.sender.event.AuthReq;
import mobi.sender.event.ChatHoleReq;
import mobi.sender.event.ChatInfoReq;
import mobi.sender.event.ChatUpdatedEvent;
import mobi.sender.event.Code3Event;
import mobi.sender.event.CompFormUpdatedEvent;
import mobi.sender.event.CreateGroupChatReq;
import mobi.sender.event.DelFromChatReq;
import mobi.sender.event.DeleteMyAvatarReq;
import mobi.sender.event.DisconnectReq;
import mobi.sender.event.EditMsgReq;
import mobi.sender.event.EnableGpsEvent;
import mobi.sender.event.GetCompFormReq;
import mobi.sender.event.GetCompanyOperatorsReq;
import mobi.sender.event.GetCtByPhoneReq;
import mobi.sender.event.GetHistoryReq;
import mobi.sender.event.GetMyInfoReq;
import mobi.sender.event.GetStorageReq;
import mobi.sender.event.GetUserInfoReq;
import mobi.sender.event.LeaveChatReq;
import mobi.sender.event.MsgUpdatedEvent;
import mobi.sender.event.OnLocationChangedEvent;
import mobi.sender.event.P24ChangeBtcEvent;
import mobi.sender.event.P24DisableDeviceReq;
import mobi.sender.event.P24onRegEvent;
import mobi.sender.event.RefreshTokenEvent;
import mobi.sender.event.RegEvent;
import mobi.sender.event.SearchReq;
import mobi.sender.event.SendFileReq;
import mobi.sender.event.SendFormReq;
import mobi.sender.event.SendGAuthTokenReq;
import mobi.sender.event.SendLocationReq;
import mobi.sender.event.SendOnlineReq;
import mobi.sender.event.SendQrReq;
import mobi.sender.event.SendReadReq;
import mobi.sender.event.SendStickerReq;
import mobi.sender.event.SendTextReq;
import mobi.sender.event.SendTypingReq;
import mobi.sender.event.SetActStateReq;
import mobi.sender.event.SetChatOptionsReq;
import mobi.sender.event.SetChatProfileReq;
import mobi.sender.event.SetDialogCryptStateReq;
import mobi.sender.event.SetFullVerReq;
import mobi.sender.event.SetMyInfoReq;
import mobi.sender.event.SetOperOnlineReq;
import mobi.sender.event.SetStorageReq;
import mobi.sender.event.SetlocaleReq;
import mobi.sender.event.StatusInternetEvent;
import mobi.sender.event.StopReq;
import mobi.sender.event.SyncDlgReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.event.TypingEvent;
import mobi.sender.event.UndefinedEvent;
import mobi.sender.event.UpdateBarEvent;
import mobi.sender.event.UpdateContacеReq;
import mobi.sender.event.UpdateCtReq;
import mobi.sender.event.UploadFileReq;
import mobi.sender.model.ChatBased;
import mobi.sender.model.Dialog;
import mobi.sender.model.StateHolder;
import mobi.sender.model.User;
import mobi.sender.model.listener.OnLoadListeners;
import mobi.sender.model.msg.MediaMessage;
import mobi.sender.model.msg.MsgBased;
import mobi.sender.model.msg.TextMessage;
import mobi.sender.tool.CtConnector;
import mobi.sender.tool.HisReq;
import mobi.sender.tool.HistoryLoader;
import mobi.sender.tool.IvrPhoneStateListener;
import mobi.sender.tool.LWallet;
import mobi.sender.tool.MsgCryptFacade;
import mobi.sender.tool.Notificator;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.tool.bar.SendBar;
import mobi.sender.tool.gcm.GCMHelper;
import mobi.sender.tool.gif.GifDecoder;
import mobi.sender.tool.utils.MediaUtils;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.SyncBtcActivity;

public class Sender extends Service implements Bus.Subscriber {

    // todo: перевесит сообщения на системные
    public static List<String> p24Classes = Arrays.asList("570f88c582ba96058fca13fc.102912.privat24"
            , "57a3181960e32734ba056f49.153508.privat24"
            , "57b1ae18f6c3760bc35532e6.155634.privat24"
            , ".cardSharing.sender");

    public static final String ALL_CHAT_ID = "*";
    private static final String NTF_FML = "{\"type\":\"col\",\"bg\":\"#E6616161\",\"weight\":\"100\",\"pd\":[4,4,4,4],\"items\":[{\"weight\":\"100\",\"type\":\"text\",\"val\":\"%s\",\"size\":\"10\",\"color\":\"#FFFFFF\",\"talign\":\"center\"}]}";
    private ChatFacade chat;
    private boolean active = false;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private String mChatId;
    private LocationManager lm;
    private LocationListener locationListener;
    private static final boolean NEED_STOP_COMET = true;

    private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String strAction = intent.getAction();
            Tool.log("== SCREEN RECIVER!");
            if (strAction.equals(Intent.ACTION_SCREEN_OFF) && NEED_STOP_COMET) {
                Tool.log("DisconnectRequest @@@");
                if (chat != null) {
                    chat.stop();
                    chat = null;
                    Tool.log("chat == null @@@");
                }
            } else if (strAction.equals(Intent.ACTION_SCREEN_ON) && isDisplayedApplication(Sender.this)) {
                onStartLogic();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Tool.log("===onStartCommand intent = " + intent + ", flags = " + flags + ", startID = " + startId);
        onStartLogic();
//        return START_STICKY;
        return START_NOT_STICKY;
    }

    private void onStartLogic() {

        if (chat == null) {
            Tool.log("chat is null. Init...");
            init();
        } else {
            Tool.log("try start comet... = " + !isDisplayedApplication(this));
            Tool.log("=== startComet");
            chat.startComet(!isDisplayedApplication(this));
        }
        Bus.getInstance().register(this, AuthReq.class.getSimpleName());
        Bus.getInstance().register(this, StopReq.class.getSimpleName());
        Bus.getInstance().register(this, GetStorageReq.class.getSimpleName());
        Bus.getInstance().register(this, GetCtByPhoneReq.class.getSimpleName());
        Bus.getInstance().register(this, UpdateCtReq.class.getSimpleName());
        Bus.getInstance().register(this, Add2ChatReq.class.getSimpleName());
        Bus.getInstance().register(this, LeaveChatReq.class.getSimpleName());
        Bus.getInstance().register(this, GetMyInfoReq.class.getSimpleName());
        Bus.getInstance().register(this, SetStorageReq.class.getSimpleName());
        Bus.getInstance().register(this, GetHistoryReq.class.getSimpleName());
        Bus.getInstance().register(this, SendTextReq.class.getSimpleName());
        Bus.getInstance().register(this, SendFileReq.class.getSimpleName());
        Bus.getInstance().register(this, SearchReq.class.getSimpleName());
        Bus.getInstance().register(this, SendStickerReq.class.getSimpleName());
        Bus.getInstance().register(this, SendLocationReq.class.getSimpleName());
        Bus.getInstance().register(this, SyncDlgReq.class.getSimpleName());
        Bus.getInstance().register(this, SendOnlineReq.class.getSimpleName());
        Bus.getInstance().register(this, SetActStateReq.class.getSimpleName());
        Bus.getInstance().register(this, SetMyInfoReq.class.getSimpleName());
        Bus.getInstance().register(this, GetUserInfoReq.class.getSimpleName());
        Bus.getInstance().register(this, SetChatOptionsReq.class.getSimpleName());
        Bus.getInstance().register(this, SendFormReq.class.getSimpleName());
        Bus.getInstance().register(this, SendQrReq.class.getSimpleName());
        Bus.getInstance().register(this, SetOperOnlineReq.class.getSimpleName());
        Bus.getInstance().register(this, SetDialogCryptStateReq.class.getSimpleName());
        Bus.getInstance().register(this, EditMsgReq.class.getSimpleName());
        Bus.getInstance().register(this, SetlocaleReq.class.getSimpleName());
        Bus.getInstance().register(this, DelFromChatReq.class.getSimpleName());
        Bus.getInstance().register(this, GetCompFormReq.class.getSimpleName());
        Bus.getInstance().register(this, UploadFileReq.class.getSimpleName());
        Bus.getInstance().register(this, SendTypingReq.class.getSimpleName());
        Bus.getInstance().register(this, SendGAuthTokenReq.class.getSimpleName());
        Bus.getInstance().register(this, GetCompanyOperatorsReq.class.getSimpleName());
        Bus.getInstance().register(this, OnLocationChangedEvent.class.getSimpleName());
        Bus.getInstance().register(this, SendReadReq.class.getSimpleName());
        Bus.getInstance().register(this, SetChatProfileReq.class.getSimpleName());
        Bus.getInstance().register(this, UpdateContacеReq.class.getSimpleName());
        Bus.getInstance().register(this, DeleteMyAvatarReq.class.getSimpleName());
        Bus.getInstance().register(this, AlertReq.class.getSimpleName());
        Bus.getInstance().register(this, CreateGroupChatReq.class.getSimpleName());
        Bus.getInstance().register(this, EnableGpsEvent.class.getSimpleName());
        Bus.getInstance().register(this, DisconnectReq.class.getSimpleName());
        Bus.getInstance().register(this, RefreshTokenEvent.class.getSimpleName());
        Bus.getInstance().register(this, ChatHoleReq.class.getSimpleName());
        Bus.getInstance().register(this, SetFullVerReq.class.getSimpleName());
        Bus.getInstance().register(this, ChatInfoReq.class.getSimpleName());
        Bus.getInstance().register(this, P24DisableDeviceReq.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //get latitude and longitude
        if (Storage.getInstance(this).isGpsEnable()) {
            enableGps();
        }
        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
    }

    private void enableGps() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (lm == null) lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationListener == null) locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Storage.getInstance(getApplicationContext()).saveMyCoordinates(location.getLatitude(), location.getLongitude());
                    Storage.getInstance(getApplicationContext()).setLocation((float) location.getLatitude(), (float) location.getLongitude());
                    if (chat != null) {
                        chat.setCoords(location.getLatitude() + "", location.getLongitude() + "");
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };

            if (Storage.getInstance(this).isGpsEnable()) {
                if (lm.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 60, 0, locationListener);  //1 hour and 0 m
                if (lm.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60 * 60, 0, locationListener);
            } else {
                if (lm != null) lm.removeUpdates(locationListener);
            }
        }
    }

    private void loadUserInfo(final String userId, final OnLoadListeners listener) {
        pool.execute(new Runnable() {
            @Override
            public void run() {
                final Storage storage = Storage.getInstance(getApplicationContext());
                if (!storage.isUserExists(userId)) {
                    if (chat == null) init();
                    chat.getUserInfo(userId, new ChatFacade.JsonRespListener() {
                        @Override
                        public void onSuccess(JSONObject model) {

                            try {
                                User user = new User(model.getJSONObject("ct"));
                                storage.saveUser(user);
                                Tool.log("*** username = " + user.getName());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (listener != null) listener.onSuccess();
                        }

                        @Override
                        public void onError(Exception e, String req) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    Tool.log("User id " + userId + " exists");
                    if (listener != null) listener.onSuccess();
                }
            }
        });
    }

//    private void sendRead(String chatId) {
//        sendRead(chatId, -1);
//    }

    private void sendRead(String chatId, long top) {
        Storage storage = Storage.getInstance(this);
        if (top <= 0) top = storage.getLastPacketId(chatId);
        if (storage.getSendStatusRead()) {
            chat.sendRead(storage.getSID(), String.valueOf(top), chatId, false);
        }
        storage.setChatUnreadCount(chatId, 0);
    }

    private void getchatInfo(final String chatId) {
        getchatInfo(chatId, null);
    }

    private void getchatInfo(final String chatId, final ChatInfoListener cil) {
        chat.getChat(chatId, new ChatFacade.JsonRespListener() {
            @Override
            public void onSuccess(JSONObject model) {
                try {
                    JSONObject ch = model.getJSONObject("chat");
                    Storage storage = Storage.getInstance(getApplicationContext());
                    if (!storage.isDialogExists(chatId)) {
                        Dialog d = new Dialog();
                        if (ch.has("name")) d.setName(ch.optString("name"));
                        d.setChatId(chatId);
                        if (ch.has("photo")) d.setChatPhoto(ch.optString("photo"));
                        storage.saveDialog(d);
                    }

                    if (ch.has("phone")) {
                        storage.setUserPhone(ch.optString("chatId"), ch.optString("phone"));
                    }
                    if (ch.has("photo") && ch.getString("photo").startsWith("http")) {
                        storage.setChatPhoto(ch.optString("chatId"), ch.optString("photo"));
                    }
                    String chatName = ch.optString("name");
                    if (ch.has("members")) {
                        JSONArray members = ch.getJSONArray("members");
                        String syntName = "";
                        JSONArray ja = new JSONArray();
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject jo = members.getJSONObject(i);
                            String userId = jo.getString("userId");
                            syntName += jo.optString("name");
                            if (i < members.length() - 1) syntName += ", ";
                            loadUserInfo(userId, null);

                            JSONObject jObj = new JSONObject();
                            jObj.put("userId", jo.getString("userId"));
                            jObj.put("role", jo.getString("role"));
                            ja.put(jObj);
                        }
                        if (chatName.trim().length() == 0) {
                            storage.setDialogName(syntName, chatId, false);
                        } else {
                            storage.setDialogName(chatName, chatId, true);
                        }

                        storage.setChatMembers(chatId, ja);


                        if ("p2p".equals(ch.optString("type")) || "company".equals(ch.optString("type"))) {
                            User u = new User();
                            if (ch.has("name")) u.setName(ch.optString("name"));
                            u.setChatId(chatId);
                            if (ch.has("photo"))
                                u.setChatPhoto(ch.optString("photo"));
                            if (ch.has("type"))
                                u.setCompany("company".equals(ch.optString("type")));
                            if (ch.has("phone"))
                                u.setPhone(ch.optString("phone"));
                            storage.saveUser(u);
                        }
                    }

                    saveOptions(ch, chatId);

                    if (ch.has("bar")) {
                        String bar = ch.getJSONObject("bar").toString();
                        storage.saveBar(Tool.getUserId(chatId), bar);
                        Bus.getInstance().post(new UpdateBarEvent(chatId));
                    }

                    if (ch.has("barO")) {
                        String bar = ch.getJSONObject("barO").toString();
                        storage.saveBarO(Tool.getUserId(chatId), bar);
                        Bus.getInstance().post(new UpdateBarEvent(chatId));
                    }

                    Bus.getInstance().post(new ChatUpdatedEvent(chatId));
                    if (cil != null) cil.onInfoFinished();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e, String req) {
                e.printStackTrace();
            }
        });
    }

    private void saveOptions(JSONObject ch, String chatId) {
        try {
            if (ch.has("options")) {
                JSONObject ojo = ch.optJSONObject("options");
                boolean isBlocked = ojo.optBoolean("block", false);
                boolean isFavorite = ojo.optBoolean("fav", false);
                boolean isMute = "all".equals(ojo.getJSONObject("ntf").getString("m"));
                Storage.getInstance(this).setChatOptions(isBlocked, isFavorite, isMute, chatId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private long saveServerMessages(final String chatId, JSONArray arr, boolean fromHistory) {
        long lpi = Integer.MAX_VALUE;
        long maxPid = Integer.MIN_VALUE;
        Storage storage = Storage.getInstance(this);
        Tool.log("*** msgs length = " + arr.length());
        for (int i = 0; i < arr.length(); i++) {
            try {
                final JSONObject j = arr.getJSONObject(i);
                final long pi = j.getLong("packetId");
                if (lpi > pi) lpi = pi;
                if (maxPid < pi) maxPid = pi;

                if (j.has("linkId") && !j.optString("linkId").equals(j.optString("packetId"))) {
                    if (storage.isFormExist(j.optString("linkId"))) {
                        storage.updateServerMessage(chatId, j);
                    }
                } else {
                    // TODO: блок костылей протокола :-(

                    //FOR OPER CHATS
                    if ("text".equals(j.optString("class"))) {
                        JSONObject jo = j.optJSONObject("model");
                        if (jo.has("fromName") && jo.has("fromPhoto")) {
                            JSONArray ja = new JSONArray();
                            JSONObject jObj = new JSONObject();
                            jObj.put("fromName", jo.optString("fromName"));
                            jObj.put("fromPhoto", jo.optString("fromPhoto"));
                            jObj.put("from", j.optString("from"));
                            storage.saveOperUsers(chatId, ja.put(jObj));
                        }
                    }

                    // contact forms
                    if (j.optString("class").startsWith("contact.contact.")) {
                        if (!j.optJSONObject("view").toString().equals(storage.getCompForm(chatId).toString())) {

                            storage.saveCompForm(chatId, j.optJSONObject("view").toString());
                            Bus.getInstance().post(new CompFormUpdatedEvent(chatId));
                        }
                        continue;
                    }
                    // ...end

                    /*// set chat encrypted when status is changed...
                    boolean encrypted = false;
                    try {
                        encrypted = j.optJSONObject("model").optBoolean("encrypted") || 1 == j.optJSONObject("model").optInt("encrypted");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (storage.isChatEncrypted(chatId) != encrypted && ChatFacade.CLASS_TEXT_ROUTE.equals(j.optString("class"))) {
                        storage.setChatEncrypted(chatId, encrypted);
                    }
                    // ...end*/

                    // force open...
                    if (j.optJSONObject("model").optBoolean("forceOpen")) {
                        loadUserInfo(j.optString("from"), new OnLoadListeners() {
                            @Override
                            public void onSuccess() {
//                                Bus.getInstance().post(new ForceOpenEvent(chatId));
                                makeForceOpen(chatId);
                            }

                            @Override
                            public void onError(String message) {
                                Tool.log("onError mess = " + message);
                            }
                        });
                    }
                    // ...end

                    // fix sticker forms...
                    if (j.optString("class").endsWith(".alert.sender")) {
                        j.put("class", ChatFacade.CLASS_STICKER);
                        String src = j.getJSONObject("view").getJSONArray("items").getJSONObject(0).getString("src");
                        j.remove("view");
                        j.put("model", new JSONObject().put("id", src));
                    }
                    // ...end

                    // make form for notifications...
                    if (ChatFacade.CLASS_NTF_CHAT.equals(j.optString("class"))) {

                        JSONObject model = j.getJSONObject("model");
                        String type = model.getString("type");
                        String who = model.getJSONObject("actionUser").getString("name");
                        String whoId = model.getJSONObject("actionUser").getString("userId");
                        String myUserId = storage.getMyUserId();
                        String msg = "";
                        if (myUserId.equals(whoId) && "leave".equals(type)) {
                            Bus.getInstance().post(new ChatUpdatedEvent(chatId));
//                            continue;
                        }
                        JSONArray users = model.getJSONArray("users");
                        String whom = "";
                        for (int k = 0; k < users.length(); k++) {
                            if (myUserId.equals(users.getJSONObject(k).getString("userId"))) {
                                String lastString = whom;
                                if (users.length() == 1) {
                                    whom = getString(R.string.msg_you_2) + lastString;
                                } else {
                                    whom = getString(R.string.msg_you_and) + lastString;
                                }
                            } else {
                                whom += users.getJSONObject(k).getString("name");
                                if (k < users.length() - 1) whom += ", ";
                            }
                        }

                        String who1 = who.replaceAll("\"", "'");
                        String whom1 = whom.replaceAll("\"", "'");

                        msg = who1 + " ";
                        if ("del".equals(type)) {
                            if (myUserId.equals(whoId)) {
                                msg = getString(R.string.msg_you) + getString(R.string.msg_deleted_2) + whom1 + getString(R.string.msg_from_chat);
                            } else if (storage.getMyName().equals(whom1)) {
                                msg += getString(R.string.msg_deleted) + getString(R.string.msg_you_2) + getString(R.string.msg_from_chat);

                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                                        .setSmallIcon(Storage.getInstance(Sender.this).isEmptyAuthToken() ?
                                                R.drawable.ic_menu_dialog : R.drawable.ic_launcher_white)
                                        .setContentTitle(getString(R.string.ntf_you_removed_from_chat))
                                        .setContentText(storage.getChatName(chatId));

                                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                mNotifyMgr.notify(001, mBuilder.build());

                            } else {
                                msg += getString(R.string.msg_deleted) + whom1 + getString(R.string.msg_from_chat);
                            }
                        }
                        if ("add".equals(type)) {
                            if (who1.equals(whom1)) {
                                if (who1.equals(myUserId)) {
                                    msg = getString(R.string.msg_you_join_the_chat);
                                } else {
                                    msg = whom1 + getString(R.string.msg_joined_the_chat);
                                }
                            } else if (storage.getMyUserId().equals(whoId)) {
                                msg = getString(R.string.msg_you) + getString(R.string.msg_added_2) + whom1 + getString(R.string.msg_to_chat);
                            } else if (storage.getMyName().equals(whom1)) {
                                msg += getString(R.string.msg_added) + getString(R.string.msg_you_2) + getString(R.string.msg_to_chat);
                            } else {
                                msg += getString(R.string.msg_added) + whom1 + getString(R.string.msg_to_chat);
                            }
                        }
                        if ("leave".equals(type)) {
                            if (myUserId.equals(whoId)) {
                                msg = getString(R.string.msg_you_leave_chat);
                            } else {
                                msg += getString(R.string.msg_leave_chat);
                            }
                        }
                        j.getJSONObject("model").put("title", msg);
                        msg = String.format(NTF_FML, msg);
                        j.put("view", new JSONObject(msg));
                    }
                    // ...end

                    // make form for enable encryption...
                    if (ChatFacade.CLASS_CHAT_KEY_SET.equals(j.optString("class"))) {

                        JSONObject model = j.getJSONObject("model");
                        String senderKey = model.getString("senderKey");
                        String encrKey = model.getString("encrKey");

                        String oldSenderKey = model.getString("oldSenderKey");
                        String oldEncrKey = model.getString("oldEncrKey");

                        String who = model.getJSONObject("actionUser").getString("name");
                        String whoId = model.getJSONObject("actionUser").getString("userId");

                        if (storage.getMyUserId().equals(whoId)) {
                            who = getString(R.string.msg_i);
                        }

                        String msg = "";
                        if (!senderKey.isEmpty() && !encrKey.isEmpty()) {
                            LWallet wallet = LWallet.getInstance(this);
                            String key = wallet.decrypt(wallet.pubKeyFromString(senderKey), encrKey);
                            storage.addDialogKey(chatId, key);
                            msg = who + getString(R.string.msg_encr);
                        } else {
                            msg = who + getString(R.string.msg_encr_of);
                        }

                        if (!oldSenderKey.isEmpty() && !oldEncrKey.isEmpty()) {
                            LWallet wallet = LWallet.getInstance(this);
                            String key = wallet.decrypt(wallet.pubKeyFromString(oldSenderKey), oldEncrKey);
                            storage.addDialogKey(chatId, key);
                        }

                        msg = String.format(NTF_FML, msg);
                        j.put("view", new JSONObject(msg));
                        j.getJSONObject("model").put("title", getString(R.string.stub_form));

                        if (pi > storage.getLastPacketId(chatId)) {
                            if (encrKey.isEmpty()) {
                                storage.setChatEncrypted(chatId, false);
                            } else {
                                storage.setChatEncrypted(chatId, true);
                            }
                        }
                    }
                    // ...end

                    // make form for select theme...
                    if (ChatFacade.CLASS_NTF_THEME.equals(j.optString("class"))) {
                        JSONObject model = j.getJSONObject("model");
                        String who = model.getJSONObject("actionUser").getString("name");
                        String whoId = model.getJSONObject("actionUser").getString("userId");
                        String theme = model.getString("themeName");
                        String myUserId = storage.getMyUserId();
                        String msg = "";
                        if (whoId.equals(myUserId)) {
                            msg = getString(R.string.msg_you_set_theme) + theme + "'";
                        } else {
                            msg = who + getString(R.string.msg_select_them) + "'" + theme + "'";
                        }
                        msg = String.format(NTF_FML, msg);
                        j.put("view", new JSONObject(msg));
                        j.getJSONObject("model").put("title", getString(R.string.stub_form));
                    }
                    //...end

                    // make toOper...
                    if (j.has("toOper")) {
                        j.put("toOper", j.optBoolean("toOper") ? 1 : 0);
                    } else {
                        j.put("toOper", 0);
                    }
                    //...end

                    if (ChatFacade.CLASS_GET_SELF_INFO.equals(j.optString("class"))) {
                        saveMyInfo(j.getJSONObject("model"));
                    }

                    if (ChatFacade.CLASS_UPATE_CHATUS_INFO.equals(j.optString("class"))) {
                        saveMyInfo(j.getJSONObject("model"));
                    }

                    storage.saveServerMessage(chatId, j);
                }


                boolean isMine = storage.getMyUserId().equals(j.optString("from"));
                boolean isMute = "mute".equals(j.getJSONObject("model").optString("state"));
                final boolean isFin = "transaction".equals(j.getJSONObject("model").optString("state"));
                if (!isMine) {
                    if (active) {
                        if (mChatId != null && !mChatId.equals(chatId) && !mChatId.equals(ALL_CHAT_ID)) {
                            if (!fromHistory && !isMute && StateHolder.getInstance(this).isDialogsSynced())
                                Notificator.getInstance(this).addNotification(chatId, j.optString("from"), MsgBased.getTextFromModel(this, j), isFin, j.has("linkId") ? j.optString("linkId") : pi + "");
                        }
                    } else {
                        if (!fromHistory && !isMute && StateHolder.getInstance(this).isDialogsSynced()) {
                            if (!storage.isUserExists(Tool.getUserId(chatId)) && !storage.isDialogExists(chatId)) {
                                chat.getChat(chatId, new ChatFacade.JsonRespListener() {
                                    @Override
                                    public void onSuccess(JSONObject model) {
                                        try {
                                            JSONObject ch = model.getJSONObject("chat");
                                            Storage storage = Storage.getInstance(getApplicationContext());
                                            if ("p2p".equals(ch.optString("type")) || "company".equals(ch.optString("type"))) {
                                                User u = new User();
                                                if (ch.has("name")) u.setName(ch.optString("name"));
                                                u.setChatId(chatId);
                                                if (ch.has("photo"))
                                                    u.setChatPhoto(ch.optString("photo"));
                                                if (ch.has("type"))
                                                    u.setCompany("company".equals(ch.optString("type")));
                                                if (ch.has("phone"))
                                                    u.setPhone(ch.optString("phone"));
                                                storage.saveUser(u);
                                            }

                                            Notificator.getInstance(getApplicationContext()).addNotification(chatId, j.optString("from"), MsgBased.getTextFromModel(getApplicationContext(), j), isFin, j.has("linkId") ? j.optString("linkId") : pi + "");

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(Exception e, String req) {
                                        e.printStackTrace();
                                    }
                                });
                            } else {
                                Notificator.getInstance(getApplicationContext()).addNotification(chatId, j.optString("from"), MsgBased.getTextFromModel(getApplicationContext(), j), isFin, j.has("linkId") ? j.optString("linkId") : pi + "");
                            }
                        }
                    }
                }

                Tool.log("*** from = "+j.optString("from")+", my = "+storage.getMyUserId());
                if (!isMine && !storage.isUserExists(j.optString("from"))) {
                    getUserInfo(j.optString("from"), null);
                }
                Tool.log("*** isChatExist = "+storage.isDialogExists(chatId));
                if(!storage.isDialogExists(chatId)){
                    getchatInfo(chatId);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mChatId != null && mChatId.equals(chatId) && arr.length() > 0 && active && !storage.isLastMsgFromMe(mChatId)) {
            sendRead(chatId, maxPid);
        }
        if (arr.length() > 0) {
            Storage.getInstance(getApplicationContext()).updateLastMessage(chatId);
        }

        return lpi;
    }

    private void doFinishAuth(JSONObject jo) {
        StateHolder.getInstance(this).setRegistered();
        Storage.getInstance(this).saveUserId(jo.optString("userId"));

        startGcm();

        chat.callCompanies(new ChatFacade.JsonRespListener() {
            @Override
            public void onSuccess(JSONObject model) {
                Storage.getInstance(getApplicationContext()).saveDefCompForm(model.optJSONObject("defaultView").toString());
            }

            @Override
            public void onError(Exception e, String req) {
                e.printStackTrace();
            }
        });
    }

    private void startGcm() {
        new GCMHelper(this).getGcmId(new GCMHelper.GcmRegCallback() {
            @Override
            public boolean onRegSuccess(String regId) {
                Tool.log("try send token: " + regId + " chat: " + chat);
                if (chat != null) chat.sendToken(regId);
                return true;
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                if (chat != null)
                    chat.sendToken("error_" + e.getMessage() + "_" + System.currentTimeMillis());
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private void init() {
        Log.verbose = true;
        try {
            ChatFacade.SenderListener listener = new ChatFacade.SenderListener() {
                @Override
                public void onSysData(JSONArray arr) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            JSONObject jo = arr.getJSONObject(i);
                            processSysMessage(jo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onChatData(JSONArray arr, final String chatId, int unread, boolean more, String status) {
                    Tool.log("*** onChatData arr = " + arr + ", status = " + status);
                    Storage storage = Storage.getInstance(getApplicationContext());
                    if (status.trim().length() > 0) {
                        storage.setChatStatus(chatId, status);
                    }
                    if (unread == 0) {
                        Notificator.getInstance(getApplicationContext()).removeNotifications(chatId);
                    }
                    storage.setChatUnreadCount(chatId, unread);
                    if (arr != null) {
                        if (arr.length() > 0) {
                            sendP24Events(arr);
                            long lastPacketId = storage.getLastPacketId(chatId);
                            String top = String.valueOf(lastPacketId);
                            final long b = saveServerMessages(chatId, arr, false);
                            final String both = String.valueOf(b);
                            if (more) {
                                storage.setChatHole(chatId, top, both);
                            }
                            if (lastPacketId < 0) {
                                HistoryLoader.getInstance(Sender.this, chat, new HistoryLoader.LoadListener() {
                                    @Override
                                    public long onLoad(String chatId, JSONArray msgs) {
                                        return saveServerMessages(chatId, msgs, true);
                                    }
                                }).getHistory(new HisReq(chatId, null, both, null));
                            }
                        }
                    }
                    //It's called to refresh only status
                    Bus.getInstance().post(new MsgUpdatedEvent(chatId));
                    SenderHelper.p24getCountUnreadMessages(Sender.this);
                }

                @Override
                public void onReg(String sid, String UDID, String phonePref, String phone, String country, boolean fullVer, String auth) {
                    Storage storage = Storage.getInstance(getApplicationContext());
                    storage.setFullVer(fullVer);
                    storage.clearHistory();
                    StateHolder.getInstance(getApplicationContext()).setUnRegistered();
                    storage.savePhonePref(phonePref);
                    storage.saveSID(sid);
                    storage.saveUdid(UDID);
                    Tool.log("==== reg phone = " + phone + ", udid = " + storage.getUdid() + ", auth = " + auth);
                    if ("".equals(storage.getAuthToken())) {//Sender
                        Bus.getInstance().post(new RegEvent());
                    } else {//P24 and sso clients
                        if ("sso".equals(auth) || "auth".equals(auth)) {
                            storage.saveMyPhone(phone);
                            Bus.getInstance().post(new P24onRegEvent());
                        } else {
                            Tool.log("=== wrong reg auth = " + auth);
                            storage.saveSID(ChatFacade.SID_UNDEF);
                            fullStop();
                        }
//                        if (phone.isEmpty()) {
//                            Tool.log("=== empty");
//                            storage.saveSID(ChatFacade.SID_UNDEF);
//                            fullStop();
//                        } else {
//                            if ("".equals(Storage.getInstance(Sender.this).getAuthToken())) {
//                                doSyncDlg(null);
//                            } else {
//                                storage.saveMyPhone(phone);
//                                Bus.getInstance().post(new P24onRegEvent());
//                            }
//                        }
                    }
                }

                @Override
                public void onNeedUpdate() {
                    // TODO:
                }

                @Override
                public void onToken(final String token) {
                    // TODO:
                }

                @Override
                public void onRegError(Exception e) {
                    e.printStackTrace();
                    Tool.log("=== onRegError e = " + e);
                }

                @Override
                public void onConnected() {
                    Tool.log("== onConnected");
                    Bus.getInstance().post(new StatusInternetEvent(true));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Tool.log("== connected, check unsent messages");
                            final Storage storage = Storage.getInstance(getApplicationContext());
                            for (MsgBased m : storage.getUnsentTextMessages()) {
                                final TextMessage tm = (TextMessage) m;
                                Tool.log("== try resend msg: " + tm.getText());
                                chat.sendMessage(tm.getText(), false, tm.isEncrypted(), tm.getPkey(),
                                        String.valueOf(tm.getPacketId()), tm.getChatId(), new ChatFacade.SendMsgListener() {
                                            @Override
                                            public void onSuccess(String serverId, long time) {
                                                storage.setTextMessageSent(tm.getPacketId(), serverId, time, mChatId);
                                            }

                                            @Override
                                            public void onError(Exception e, String req) {
                                                e.printStackTrace();
                                            }
                                        });
                            }

                            for (MsgBased m : storage.getUnsentMediaMessages()) {
                                MediaMessage mm = (MediaMessage) m;
                                String type = "";
                                if ("png".equals(mm.getType())) {
                                    type = "image/png";
                                } else if ("jpeg".equals(mm.getType())) {
                                    type = "image/jpeg";
                                }
                                sendMediaFile(mm.getPacketId(), mm.getUrl(), type, mm.getChatId(), mm.getName(), mm.getDesc(), false);
                            }
                        }
                    }).start();
                }

                @Override
                public void onDisconnected() {
                    Tool.log("== onDisconnected");
                    Bus.getInstance().post(new StatusInternetEvent(false));
                }

                @Override
                public void onCode3() {
                    Bus.getInstance().post(new Code3Event());
                }
            };

            Storage storage = Storage.getInstance(this);
            Tool.log("=== init isShort = " + isDisplayedApplication(this));
            if ("".equals(storage.getAuthToken())) {
                chat = new ChatFacade(
                        ChatFacade.URL_ALPHA,
                        storage.getDevId(),
                        storage.getDevKey(),
                        storage.getSID(),
                        Tool.getImei(this),
                        Tool.getDeviceName(),
                        "phone",
                        Tool.getVersion(this),
                        10,
                        null,
                        !isDisplayedApplication(Sender.this),
                        listener);
            } else {
                chat = new ChatFacade(
                        ChatFacade.URL_ALPHA,
                        storage.getDevId(),
                        storage.getDevKey(),
                        storage.getSID(),
                        Tool.getImei(this),
                        Tool.getDeviceName(),
                        "phone",
                        Tool.getVersion(this),
                        10,
                        storage.getAuthToken(),
                        storage.getCompanyId(),
                        null,
                        !isDisplayedApplication(Sender.this),
                        listener);
            }
            float[] coor = storage.getMyCoordinates();
            chat.setCoords(coor[0] + "", coor[1] + "");
        } catch (Exception e) {
            e.printStackTrace();
            Tool.log("=== onDestroy 1");
            onDestroy();
        }
    }

    private void sendP24Events(JSONArray arr) {
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject object = arr.getJSONObject(i);
                String aclass = object.optString("class");
                if (p24Classes.contains(aclass)) {
                    Bus.getInstance().post(new UndefinedEvent(object));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isDisplayedApplication(Context ctx) {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Activity.ACTIVITY_SERVICE);
        String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        return ctx.getPackageName().equals(packageName) && ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private void processSysMessage(JSONObject jo) {
        Storage storage = Storage.getInstance(getApplicationContext());
        String className = jo.optString("class");
        switch (className) {
            case ChatFacade.CLASS_TYPING:
                try {
                    JSONObject model = jo.getJSONObject("model");
                    Bus.getInstance().post(new TypingEvent(model.optString("chatId"), model.optString("from")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case ChatFacade.CLASS_UPDATE_STORAGE:
                StateHolder.getInstance(this).setWalletUnSynced();
                if (!storage.isEmptyAuthToken()) {
                    Bus.getInstance().post(new P24ChangeBtcEvent());
                } else {
                    Intent intent = new Intent(this, SyncBtcActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            case ChatFacade.CLASS_SET_CT:
                try {
                    JSONArray arr = jo.getJSONObject("model").getJSONArray("cts");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject cj = arr.getJSONObject(i);

                        final User u = new User(cj);

//                        if (cj.has("isOwn") && cj.optBoolean("isOwn") && !storage.isLocalIdExist(u.getUserId())) {
//                            if(!"".equals(u.getChatPhoto())) {
//                                Tool.http2ByteArray(u.getChatPhoto(), new Tool.LoadListener() {
//                                    @Override
//                                    public void onLoad(byte[] data) {
//                                        if(data.length < 999999) {
//                                            CtConnector.addUserToContactBook(getBaseContext(), u, data);
//                                        }else{
//                                            CtConnector.addUserToContactBook(getBaseContext(), u, new byte[0]);
//                                        }
//                                    }
//                                });
//                            }else{
//                                CtConnector.addUserToContactBook(getBaseContext(), u, new byte[0]);
//                            }
//                        }

                        if (storage.isUserExists(u.getUserId())) {
                            User user = storage.getUser(cj.optString("userId"));

                            if (cj.has("isCompany")) user.setCompany(cj.optBoolean("isCompany"));
                            if (cj.has("phone")) user.setPhone(cj.optString("phone"));
                            if (cj.has("name")) user.setName(cj.optString("name"));
                            if (cj.has("photo")) user.setChatPhoto(cj.optString("photo"));
                            if (cj.has("isBlocked")) user.setBlocked(cj.optBoolean("isBlocked"));
                            if (cj.has("isFavorite")) user.setFavorite(cj.optBoolean("isFavorite"));
                            if (cj.has("btcAddr")) user.setBtcAddr(cj.optString("btcAddr"));
                            if (cj.has("msgKey")) user.setMsgKey(cj.optString("msgKey"));
//                            if (cj.has("isOwn")) user.setOwn(cj.optBoolean("isOwn"));

                            storage.saveUser(user);
                        } else {
                            storage.saveUser(u);
                        }

                        if (cj.has("barO")) {
                            JSONObject barO = cj.optJSONObject("barO");
                            storage.saveBarO(u.getUserId(), barO.toString());
                            Bus.getInstance().post(new UpdateBarEvent(mChatId));
                        }

                        if (cj.has("bar")) {
                            String bar = cj.getJSONObject("bar").toString();
                            storage.saveBar(Tool.getUserId(mChatId), bar);
                            Bus.getInstance().post(new UpdateBarEvent(mChatId));
                        }

                        Bus.getInstance().post(new ChatUpdatedEvent(mChatId));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case ChatFacade.CLASS_AUTH:
                if (ChatFacade.AUTH_STEP_FINISH.equals(jo.optJSONObject("model").optString("step"))) {
                    doFinishAuth(jo.optJSONObject("model"));
                }
                Bus.getInstance().post(new AuthEvent(jo.optJSONObject("model")));
                break;
            case ChatFacade.CLASS_UPDATE_CHAT:
                try {
                    JSONArray chats = jo.optJSONObject("model").optJSONArray("chatList");
                    for (int i = 0; i < chats.length(); i++) {
                        JSONObject ch = chats.getJSONObject(i);
                        Dialog dialog = new Dialog(ch);
                        if (ch.has("members")) {
                            JSONArray members = ch.getJSONArray("members");
                            String syntName = "";
                            JSONArray ja = new JSONArray();
                            List<String> listUserIds = new ArrayList<>();
                            for (int j = 0; j < members.length(); j++) {
                                JSONObject jou = members.getJSONObject(j);
                                String userId = jou.getString("userId");
                                listUserIds.add(userId);

                                JSONObject jObj = new JSONObject();
                                jObj.put("userId", jou.getString("userId"));
                                jObj.put("role", jou.getString("role"));
                                ja.put(jObj);

                                syntName += jou.optString("name");
                                if (i < members.length() - 1) syntName += ", ";
                                loadUserInfo(userId, null);
                            }
                            if (dialog.getName() == null || dialog.getName().length() == 0) {
                                dialog.setName(syntName);
                            }

                            //delete dialog
//                            if (!listUserIds.contains(storage.getMyUserId())) {
//                                storage.deleteDialog(ch.optString("chatId"));
//                            }
                            if (!listUserIds.contains(storage.getMyUserId())) {
                                storage.setDialogsThatIleft(ch.optString("chatId"), true);
                            } else {
                                storage.setDialogsThatIleft(ch.optString("chatId"), false);
                            }
                            storage.saveDialog(dialog);
                            storage.setChatMembers(ch.optString("chatId"), ja);
                        }
                        Bus.getInstance().post(new ChatUpdatedEvent(ch.optString("chatId")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ChatFacade.CLASS_CHAT_SET_OPTION:
                try {
                    JSONObject joModel = jo.optJSONObject("model");
                    boolean block = joModel.getBoolean("block");
                    boolean favorite = joModel.getBoolean("fav");
                    String id = joModel.getString("id");
                    boolean mute = "all".equals(joModel.getJSONObject("ntf").getString("m"));
                    storage.setChatOptions(block, favorite, mute, id);
                    Bus.getInstance().post(new MsgUpdatedEvent(id));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ChatFacade.CLASS_FORCE_OPEN:
                try {
                    JSONObject joModel = jo.optJSONObject("model");
                    if (joModel.has("forceOpen") && joModel.optBoolean("forceOpen")) {
                        if (joModel.has("chatId")) {
//                            Bus.getInstance().post(new ForceOpenEvent(joModel.optString("chatId")));
                            makeForceOpen(joModel.optString("chatId"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                Bus.getInstance().post(new UndefinedEvent(jo));
        }
    }

    @Override
    public void onDestroy() {
        Tool.log("===onDestroy");
        Bus.getInstance().unregister(this);
        try {
            unregisterReceiver(screenOffReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (lm != null) lm.removeUpdates(locationListener);
        }
        super.onDestroy();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Tool.log("=== onTaskRemoved");
        if (chat != null) {
            chat.stop();
            chat = null;
            Tool.log("chat == null @@@");
        }
    }

    @Override
    public void onEvent(final Bus.Event evt) {
        final Storage storage = Storage.getInstance(getApplicationContext());
        if (chat == null) init();
        if (evt instanceof SetFullVerReq) {
            final SetFullVerReq event = (SetFullVerReq) evt;
            chat.callFullVersion(event.isFullVer(), new ChatFacade.JsonRespListener() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    event.getListener().onResponse(jsonObject);
                }

                @Override
                public void onError(Exception e, String s) {
                    event.getListener().onError(e);
                    Tool.log("=== onError e = " + e + ", s = " + s);
                }
            });
        }
        if (evt instanceof GetCompanyOperatorsReq) {
            final GetCompanyOperatorsReq event = (GetCompanyOperatorsReq) evt;
            chat.getCompanyOperators(event.getCompId(), new ChatFacade.JsonRespListener() {
                @Override
                public void onSuccess(JSONObject model) {
                    event.getListener().onResponse(model);
                }

                @Override
                public void onError(Exception e, String req) {
                    event.getListener().onError(e);
                }
            });
        } else if (evt instanceof SendGAuthTokenReq) {
            chat.setGoogle2faToken(((SendGAuthTokenReq) evt).getToken());
        } else if (evt instanceof SendTypingReq) {
            chat.sendTyping(((SendTypingReq) evt).getChatId());
        } else if (evt instanceof UploadFileReq) {
            try {
                String path = ((UploadFileReq) evt).getPath();
                InputStream is = new FileInputStream(path);
                String fileType = "";
                if (path.contains("."))
                    fileType = path.substring(path.lastIndexOf('.') + 1, path.length());
                chat.uploadFile(is, fileType, ChatFacade.TARGET_UPLOAD, new ChatFacade.UploadFileListener() {
                    @Override
                    public void onSuccess(String url) {
                        try {
                            ((UploadFileReq) evt).getListener().onResponse(new JSONObject().put("url", url));
                        } catch (Exception e) {
                            ((UploadFileReq) evt).getListener().onError(e);
                        }
                    }

                    @Override
                    public void onError(Exception e, String s) {
                        ((UploadFileReq) evt).getListener().onError(e);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (evt instanceof GetCompFormReq) {
            GetCompFormReq event = (GetCompFormReq) evt;
            String userId = event.getChatId().substring(User.P2P_CHAT_PREFIX.length());
            chat.callCompany(event.getChatId(), userId, "");
        } else if (evt instanceof SetlocaleReq) {
            chat.sendLocale(((SetlocaleReq) evt).getLocale());
        } else if (evt instanceof EditMsgReq) {
            final EditMsgReq event = (EditMsgReq) evt;
            TextMessage tm = storage.editTextMessage(event.getText(), event.getPacketId(), event.getChatId());

            String text = tm.getText();
            //if we delete encr message
            if ("".equals(event.getText())) {
                tm.setEncrypted(false);
                text = event.getText();
            }

            chat.editMessage(text, String.valueOf(event.getPacketId()), false, tm.isEncrypted(), tm.getPkey(), String.valueOf(System.currentTimeMillis()), event.getChatId(), new ChatFacade.EditMsgListener() {
                @Override
                public void onError(Exception e) {
                    event.getListener().onError(e);
                }

                @Override
                public void onSuccess(JSONObject jo) {
                    storage.setTextMessageSent(event.getPacketId());
                    event.getListener().onResponse(jo);
                    Bus.getInstance().post(new MsgUpdatedEvent(event.getChatId()));
                }
            });

        } else if (evt instanceof SetDialogCryptStateReq) {
            SetDialogCryptStateReq event = (SetDialogCryptStateReq) evt;
            setChatEncrypted(event.getChatId(), event.isEncrypted());

        } else if (evt instanceof SetOperOnlineReq) {
            SetOperOnlineReq event = (SetOperOnlineReq) evt;
            for (String cid : storage.getCompaniesId()) {
                chat.setCompanyStatus(storage.getSID(), cid, event.isOnline(), new ChatFacade.JsonRespListener() {
                    @Override
                    public void onSuccess(JSONObject model) {
                    }

                    @Override
                    public void onError(Exception e, String req) {
                        e.printStackTrace();
                    }
                });
            }

        } else if (evt instanceof SendQrReq) {
            final SendQrReq event = (SendQrReq) evt;
            chat.sendQR(event.getQr(), new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    event.getListener().onResponse(jsonObject);
                }

                @Override
                public void onError(Exception e) {
                    event.getListener().onError(e);
                }
            });
        } else if (evt instanceof SendFormReq) {
            SendFormReq event = (SendFormReq) evt;
            try {
                chat.sendForm(storage.getSID(), event.getModel(), event.getClassName(), event.getChatId(), event.getProcId(), new ChatFacade.SendMsgListener() {
                    @Override
                    public void onSuccess(String s, long l) {
                        // TODO:
                    }

                    @Override
                    public void onError(Exception e, String s) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (evt instanceof SetChatOptionsReq) {
            SetChatOptionsReq event = (SetChatOptionsReq) evt;
            storage.setChatOptions(event.isBlock(), event.isFavorite(), event.isMute(), event.getChatId());
            this.chat.setChatOptions(event.getChatId(), event.isFavorite(), event.isBlock(), event.isMute());

        } else if (evt instanceof GetUserInfoReq) {
            final GetUserInfoReq event = (GetUserInfoReq) evt;
            getUserInfo(event.getUserId(), event.getListener());
        } else if (evt instanceof SetMyInfoReq) {
            final SetMyInfoReq event = (SetMyInfoReq) evt;
            InputStream is = null;
            final String fileName = event.getPhoto();
            if (fileName != null) {
                Uri uri = Uri.parse(fileName);
                try {
                    is = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    try {
                        is = new FileInputStream(fileName);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            chat.setMySelfData(event.getName(), null, is, "png", event.getDesc(), null, null, new ChatFacade.SetSelfListener() {
                @Override
                public void onSetSuccess() {
                    storage.saveMyDesc(event.getDesc());
                    storage.saveMyName(event.getName());
                }

                @Override
                public void onSuccess(String url) {
                    storage.saveMyPhoto(url);
                    event.getListener().onResponse(new JSONObject());
                }

                @Override
                public void onError(Exception e, String req) {
                    e.printStackTrace();
                }
            });
        } else if (evt instanceof SetActStateReq) {
            SetActStateReq event = (SetActStateReq) evt;
            active = event.isActive();
            mChatId = event.getChatId();
            if (!mChatId.equals(ALL_CHAT_ID)) {
                chat.setActiveChat(mChatId);
            }
        } else if (evt instanceof SendOnlineReq) {
            chat.sendOnline(((SendOnlineReq) evt).getKey());
        } else if (evt instanceof SyncDlgReq) {
            doSyncDlg((SyncDlgReq) evt);
        } else if (evt instanceof SendLocationReq) {
            SendLocationReq event = (SendLocationReq) evt;
            storage.saveNewLocationMessage(event.getDescription(), event.getChatId(), event.getLat(), event.getLon());
            chat.sendFile2Chat(storage.getSID(), null, null, "", false,
                    event.getDescription(), "",
                    event.getChatId(),
                    event.getLat(),
                    event.getLon(),
                    new ChatFacade.SendFileListener() {
                        @Override
                        public void onSuccess(String s, long l, String s1, String s2, String s3) {

                        }

                        @Override
                        public void onError(Exception e, String s) {
                            e.printStackTrace();
                        }
                    });
            Bus.getInstance().post(new MsgUpdatedEvent(event.getChatId()));
        } else if (evt instanceof SendStickerReq) {
            SendStickerReq event = (SendStickerReq) evt;
            storage.saveNewStickerMessage(event.getId(), event.getChatId());
            Bus.getInstance().post(new MsgUpdatedEvent(event.getChatId()));
            chat.sendSticker(storage.getSID(), event.getId(), event.getChatId(), new ChatFacade.SendMsgListener() {
                @Override
                public void onSuccess(String s, long l) {
                }

                @Override
                public void onError(Exception e, String s) {
                    e.printStackTrace();
                }
            });
        } else if (evt instanceof SearchReq) {
            final SearchReq event = (SearchReq) evt;
            chat.searchCompanies(event.getText(), new ChatFacade.JsonRespListener() {
                @Override
                public void onSuccess(JSONObject model) {
                    event.getListener().onResponse(model);
                }

                @Override
                public void onError(Exception e, String req) {
                    event.getListener().onError(e);
                }
            });
        } else if (evt instanceof SendFileReq) {
            final SendFileReq e = (SendFileReq) evt;
            sendMediaFile(0, e.getPath(), e.getType(), e.getChatId(), e.getName(), e.getDescription(), true);
        } else if (evt instanceof SendTextReq) {
            final SendTextReq event = (SendTextReq) evt;
//            final TextMessage tm = storage.saveNewTextMessage(event.getText(), event.getChatId(), event.isToOper());
//            storage.setChatStatus(event.getChatId(), ChatBased.STATUS_STR_NONE);
//            Bus.getInstance().post(new MsgUpdatedEvent(event.getChatId()));

            chat.sendMessage(event.getText(), event.isToOper(), event.isEncr(), event.getpKey(),
                    String.valueOf(event.getPacketId()), event.getChatId(), new ChatFacade.SendMsgListener() {
                        @Override
                        public void onSuccess(String serverId, long time) {
                            storage.setTextMessageSent(event.getPacketId(), serverId, time, event.getChatId());
                            Bus.getInstance().post(new MsgUpdatedEvent(event.getChatId()));
                            event.getListener().onResponse(new JSONObject());
                        }

                        @Override
                        public void onError(Exception e, String req) {
                            event.getListener().onError(e);
                        }
                    });
        } else if (evt instanceof GetHistoryReq) {
            final GetHistoryReq event = (GetHistoryReq) evt;
            HistoryLoader hl = HistoryLoader.getInstance(Sender.this, chat, new HistoryLoader.LoadListener() {
                @Override
                public long onLoad(String chatId, JSONArray msgs) {

                    return saveServerMessages(chatId, msgs, true);
                }
            });

            hl.getHistory(new HisReq(event.getChatId(), event.getTop(), event.getBoth(), event.getListener()));

            if (!Storage.getInstance(this).isOperChat(event.getChatId())) {
                getchatInfo(event.getChatId());
            }
        } else if (evt instanceof SetStorageReq) {
            final SetStorageReq event = (SetStorageReq) evt;
            chat.setStorage(event.getStor(), new ChatFacade.JsonRespListener() {
                @Override
                public void onSuccess(JSONObject jo) {
                    String pubKey = LWallet.getInstance(getApplicationContext()).getMyRootPubKey();
                    chat.setMySelfData(null, null, null, null, null, pubKey, null, new ChatFacade.SetSelfListener() {
                        @Override
                        public void onSetSuccess() {
                        }

                        @Override
                        public void onSuccess(String url) {
                        }

                        @Override
                        public void onError(Exception e, String req) {
                            e.printStackTrace();
                        }
                    });
                    event.getListener().onResponse(jo);
                }

                @Override
                public void onError(Exception e, String s) {
                    event.getListener().onError(e);
                }
            });
        } else if (evt instanceof GetMyInfoReq) {
            final GetMyInfoReq event = (GetMyInfoReq) evt;
            this.chat.getMySelfData(new ChatFacade.JsonRespListener() {
                @Override
                public void onSuccess(JSONObject model) {
                    Tool.log("=== onSuccess = " + model);
                    saveMyInfo(model);
                    event.getListener().onResponse(model);
                }

                @Override
                public void onError(Exception e, String req) {
                    Tool.log("=== error e = " + e + ", req = " + req);
                    event.getListener().onError(e);
                }
            });
        } else if (evt instanceof DisconnectReq) {
            Tool.log("DisconnectRequest @@@");
            if (chat != null) {
                chat.stop();
                chat = null;
                Tool.log("chat == null @@@");
            }
        } else if (evt instanceof LeaveChatReq) {
            LeaveChatReq event = (LeaveChatReq) evt;
            storage.deleteDialog(event.getChatId());
            storage.setDialogsThatIleft(event.getChatId(), true);
            chat.leaveChat(event.getChatId());

        } else if (evt instanceof DelFromChatReq) {
            final DelFromChatReq event = (DelFromChatReq) evt;
            try {
                chat.delFromChat(event.getChatId(), event.getUserId(), new ChatFacade.JsonRespListener() {
                    @Override
                    public void onSuccess(JSONObject jo) {
                        event.getListener().onResponse(jo);
                        if (storage.isChatEncrypted(event.getChatId()))
                            setChatEncrypted(event.getChatId(), true);
                    }

                    @Override
                    public void onError(Exception e, String s) {
                        event.getListener().onError(e);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (evt instanceof Add2ChatReq) {
            final Add2ChatReq event = (Add2ChatReq) evt;
            try {
                JSONArray arr = event.getArr();
                String[] idList = new String[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    idList[i] = arr.getString(i);
                }
                chat.addToChat(event.getChatId(), idList, new ChatFacade.JsonRespListener() {
                    @Override
                    public void onSuccess(JSONObject jo) {
                        Dialog d = new Dialog(jo);
                        storage.saveDialog(d);
                        if (!Storage.getInstance(Sender.this).isOperChat(event.getChatId())) {
                            getchatInfo(d.getChatId(), new ChatInfoListener() {
                                @Override
                                public void onInfoFinished() {
                                    if (storage.isChatEncrypted(event.getChatId()))
                                        setChatEncrypted(event.getChatId(), true);
                                }
                            });
                        }
                        event.getListener().onResponse(jo);
                    }

                    @Override
                    public void onError(Exception e, String s) {
                        event.getListener().onError(e);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (evt instanceof UpdateCtReq) {
            final UpdateCtReq event = (UpdateCtReq) evt;
            try {
                JSONObject jo = new JSONObject()
                        .put("userId", event.getUserId())
                        .put("name", event.getName())
                        .put("isOwn", event.isAdded());
                JSONArray arr = new JSONArray().put(jo);
                chat.updateContacts(arr, new ChatFacade.JsonRespListener() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        event.getListener().onResponse(jsonObject);
                    }

                    @Override
                    public void onError(Exception e, String s) {
                        event.getListener().onError(e);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (evt instanceof GetCtByPhoneReq) {
            final GetCtByPhoneReq event = (GetCtByPhoneReq) evt;
            chat.getContactsByPhone(event.getPhone(), new ChatFacade.JsonRespListener() {
                @Override
                public void onSuccess(JSONObject jo) {
                    event.getListener().onResponse(jo);
                }

                @Override
                public void onError(Exception e, String s) {
                    event.getListener().onError(e);
                }
            });
        } else if (evt instanceof GetStorageReq) {
            final GetStorageReq event = (GetStorageReq) evt;
            chat.getStorage(new ChatFacade.StorageGetListener() {
                @Override
                public void onSuccess(String s) {
                    try {
                        event.getListener().onResponse(new JSONObject().put("storage", s));
                    } catch (JSONException e) {
                        event.getListener().onError(e);
                    }
                }

                @Override
                public void onError(Exception e, String s) {
                    e.printStackTrace();
                    try {
                        if (e.getMessage() != null && e.getMessage().contains("\"code\":2")) {
                            storage.unavtorize();
                            chat.stop();
                            event.getListener().onResponse(new JSONObject().put("storage", "unreg"));
                        } else {
//                                event.getListener().onResponse(new JSONObject().put("storage", ""));
                            event.getListener().onResponse(new JSONObject().put("storage", SyncBtcActivity.NO_INTERNET));
                        }
                    } catch (JSONException e2) {
                        event.getListener().onError(e2);
                    }
                }
            });
        } else if (evt instanceof StopReq) {
            chat.stop();
            chat = null;
            onDestroy();
        } else if (evt instanceof AuthReq) {
            final AuthReq event = (AuthReq) evt;
            chat.nativeAuth(event.getAction(), event.getParam(), null, new ChatFacade.AuthListener() {
                @Override
                public void onSuccess(String step, String s1, String device, String s3, JSONObject jo) {
                    if (ChatFacade.AUTH_STEP_LIGHT_IVR.equalsIgnoreCase(step)) {
                        final String prefix = jo.optString("prefix");
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    TelephonyManager tManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                                    IvrPhoneStateListener phoneListener = new IvrPhoneStateListener(getApplicationContext(), prefix);
                                    tManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
                                } catch (Exception e) {
                                    Tool.log("IVR auth failed: cannot listen phone state!: " + e.toString());
                                    e.printStackTrace();
                                    chat.nativeAuth(ChatFacade.AUTH_ACTION_BREAK, null, null, null);
                                }
                            }
                        });
                    }
                    if (ChatFacade.AUTH_STEP_FINISH.equals(jo.optString("step"))) {
                        doFinishAuth(jo);
                    }
                    event.getListener().onResponse(jo);
                }

                @Override
                public void onError(Exception e, String s) {
                    e.printStackTrace();
                    event.getListener().onError(e);
                }
            });
        } else if (evt instanceof SendReadReq) {
            SendReadReq event = (SendReadReq) evt;
            sendRead(event.getChatId(), event.getPacketId());
//            chat.sendRead(String.valueOf(event.getPacketId()), event.getChatId(), false);
        } else if (evt instanceof SetChatProfileReq) {
            final SetChatProfileReq e = (SetChatProfileReq) evt;

            InputStream is = null;
            String fileName = e.getPhoto();
            if (fileName != null && !fileName.startsWith("http")) {
                Uri uri = Uri.parse(fileName);
                try {
                    is = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException exp) {
                    try {
                        is = new FileInputStream(fileName);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            //TODO: костыль пришлось делать, т.к серверу нужно слать все поля
            if (fileName != null && fileName.startsWith("http")) {
                chat.setChatProfile(storage.getSID(), fileName, "png", e.getName(), null, e.getChatId(), new ChatFacade.UploadFileListener() {
                    @Override
                    public void onSuccess(String s) {
                        Bus.getInstance().post(new ChatUpdatedEvent(e.getChatId()));
                    }

                    @Override
                    public void onError(Exception e, String s) {
                        Tool.log("=== onError e = " + e);
                    }
                });
            } else {
                chat.setChatProfile(storage.getSID(), is, "png", e.getName(), null, e.getChatId(), new ChatFacade.UploadFileListener() {
                    @Override
                    public void onSuccess(String s) {
                        Bus.getInstance().post(new ChatUpdatedEvent(e.getChatId()));
                    }

                    @Override
                    public void onError(Exception e, String s) {
                        Tool.log("=== onError e = " + e);
                    }
                });
            }


        } else if (evt instanceof UpdateContacеReq) {
            try {
                final UpdateContacеReq e = (UpdateContacеReq) evt;
                JSONArray ja = new JSONArray();
                ja.put(new JSONObject().put("userId", e.getUserId()).put("name", e.getName()));
                chat.updateContacts(ja, new ChatFacade.JsonRespListener() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        Bus.getInstance().post(new ChatUpdatedEvent(e.getChatId()));
                    }

                    @Override
                    public void onError(Exception e, String s) {

                    }
                });
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        } else if (evt instanceof DeleteMyAvatarReq) {
            final DeleteMyAvatarReq e = (DeleteMyAvatarReq) evt;
            chat.delMyAvatar(new ChatFacade.SetSelfListener() {
                @Override
                public void onSetSuccess() {
                    Storage.getInstance(Sender.this).saveMyPhoto("");
                    e.getListener().onResponse(new JSONObject());
                }

                @Override
                public void onSuccess(String url) {
                }

                @Override
                public void onError(Exception e, String s) {
                }
            });
        } else if (evt instanceof AlertReq) {
            final AlertReq e = (AlertReq) evt;
            chat.sendAlert(e.getChatId());
        } else if (evt instanceof CreateGroupChatReq) {
            final CreateGroupChatReq e = (CreateGroupChatReq) evt;
            InputStream is = MediaUtils.getInputStreamFromUri(this, e.getPhotoUri());

            chat.setChat(storage.getSID(), is, e.getName(), e.getArrayUserId(), new ChatFacade.JsonRespListener() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    e.getListener().onResponse(jsonObject);
                }

                @Override
                public void onError(Exception ex, String s) {
                    e.getListener().onError(ex);
                }
            });
        } else if (evt instanceof EnableGpsEvent) {
            enableGps();
        } else if (evt instanceof RefreshTokenEvent) {
            startGcm();
        } else if (evt instanceof ChatHoleReq) {
            final ChatHoleReq event = (ChatHoleReq) evt;
            HistoryLoader hl = HistoryLoader.getInstance(this, chat, new HistoryLoader.LoadListener() {
                @Override
                public long onLoad(String chatId, JSONArray msgs) {

                    return saveServerMessages(chatId, msgs, true);
                }
            });

            hl.getChatHole(new HisReq(event.getChatId(), event.getTop() + "", event.getBottom() + "", event.getListener()));
        } else if (evt instanceof ChatInfoReq) {
            final ChatInfoReq event = (ChatInfoReq) evt;
            chat.getChat(event.getChatId(), new ChatFacade.JsonRespListener() {
                @Override
                public void onSuccess(JSONObject model) {
                    try {
                        JSONObject ch = model.getJSONObject("chat");
                        Storage storage = Storage.getInstance(getApplicationContext());
                        if ("p2p".equals(ch.optString("type")) || "company".equals(ch.optString("type"))) {
                            User u = new User();
                            if (ch.has("name")) u.setName(ch.optString("name"));
                            u.setChatId(event.getChatId());
                            if (ch.has("photo"))
                                u.setChatPhoto(ch.optString("photo"));
                            if (ch.has("type"))
                                u.setCompany("company".equals(ch.optString("type")));
                            if (ch.has("phone"))
                                u.setPhone(ch.optString("phone"));
                            storage.saveUser(u);
                        }

                        event.getListener().onResponse(model);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        event.getListener().onError(e);
                    }
                }

                @Override
                public void onError(Exception e, String req) {
                    e.printStackTrace();
                }
            });
        } else if (evt instanceof P24DisableDeviceReq) {

            final P24DisableDeviceReq event = (P24DisableDeviceReq) evt;
            chat.callDisable(Storage.getInstance(Sender.this).getDevId(),
                    Storage.getInstance(Sender.this).getUdid(),
                    Tool.getImei(this),
                    Tool.getDeviceName(),
                    "phone",
                    Tool.getVersion(this),
                    new ChatFacade.JsonRespListener() {
                        @Override
                        public void onSuccess(JSONObject jsonObject) {
                            event.getListener().onResponse(jsonObject);
                        }

                        @Override
                        public void onError(Exception e, String s) {
                            event.getListener().onError(e);
                        }
                    });
        }
    }

    private void saveMyInfo(JSONObject model) {
        Storage storage = Storage.getInstance(this);
        storage.saveMyDesc(model.optString("description"));
        storage.saveMyName(model.optString("name"));
        storage.saveMyPhoto(model.optString("photo"));
        storage.saveUserId(model.optString("userId"));
        JSONArray cts = model.optJSONArray("contacts");
        for (int i = 0; i < cts.length(); i++) {
            JSONObject cjo = cts.optJSONObject(i);
            if ("phone".equals(cjo.optString("type"))) {
                String phone = cjo.optString("value");
                if (phone.trim().length() == 0) {
//                    Storage.getInstance(this).clearHistory();
//                    StateHolder.getInstance(this).setUnRegistered();
//                    Bus.getInstance().post(new RegEvent());
                } else {
                    if (!phone.startsWith("+")) phone = "+" + phone;
                    storage.saveMyPhone(phone);
                }
                break;
            }
        }
        Set<String> companiesId = new HashSet<>();
        JSONArray jsonArray = model.optJSONArray("companies");
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    companiesId.add(jsonArray.get(i).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        storage.saveCompaniesId(companiesId);
    }


    private void setChatEncrypted(String chatId, boolean isEncrypted) {
        Storage storage = Storage.getInstance(this);
        JSONObject jo = new JSONObject();
        if (isEncrypted) {
            jo = new MsgCryptFacade(this).initEncryptGroupChat(chatId);
        } else {
            storage.setChatEncrypted(chatId, false);
        }
        LWallet wallet = LWallet.getInstance(this);
        String pubKey = null;
        if (isEncrypted) {
            pubKey = wallet.getMyRootPubKey();
        }

        chat.setGroupChatEncryption(chatId, isEncrypted, pubKey, jo, new ChatFacade.EncryptionListener() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void doSyncDlg(final SyncDlgReq evt) {
        final Storage storage = Storage.getInstance(getApplicationContext());
        JSONArray arr = new JSONArray();
        List<User> localUsers = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_DENIED) {
            localUsers = CtConnector.getLocalUsers(getApplicationContext());
            for (User u : localUsers) {
                try {
                    JSONObject jo = new JSONObject()
                            .put("localId", u.getLocalId())
                            .put("name", u.getName())
                            .put("phones", new JSONArray().put(u.getPhone()));
                    arr.put(jo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        final List<User> finalLocalUsers = localUsers;

        chat.syncData(arr, evt.isFullVersion(), new ChatFacade.JsonRespListener() {
                    @Override
                    public void onSuccess(JSONObject jo) {
                        //refresh chats that i left
                        storage.clearChatsThatILeft();

                        JSONObject bar = jo.optJSONObject("defaultBar");
                        storage.saveDefBar(bar != null ? bar.toString() : SendBar.DEFAULT_SB);
                        JSONArray chats = jo.optJSONArray("chats");
                        for (int i = 0; i < chats.length(); i++) {
                            try {
                                JSONObject ijo = chats.getJSONObject(i);
                                if ("group".equals(ijo.optString("type"))) {
                                    Dialog d = new Dialog(ijo);
                                    Tool.log("&&& saveDialog2");
                                    storage.saveDialog(d);
                                    if (!d.getSenderKey().isEmpty()) {
                                        LWallet wallet = LWallet.getInstance(getApplicationContext());
                                        String key = wallet.decrypt(wallet.pubKeyFromString(d.getSenderKey()), d.getEncrKey());
                                        storage.addDialogKey(d.getChatId(), key);
                                    }

                                    if (d.getName().trim().length() == 0) {
                                        getchatInfo(d.getChatId());
                                    }
                                } else {
                                    User u = new User(ijo);
                                    for (User lu : finalLocalUsers) {
                                        if (lu.getLocalId().equals(u.getLocalId())) {
                                            u.setPhone(lu.getPhone());
                                            break;
                                        }
                                    }
                                    storage.saveUser(u);

                                    if (ijo.has("barO")) {
                                        JSONObject barO = ijo.optJSONObject("barO");
                                        storage.saveBarO(u.getUserId(), barO.toString());
                                    }

                                    if (ijo.has("bar")) {
                                        String bar_ = ijo.getJSONObject("bar").toString();
                                        storage.saveBar(Tool.getUserId(mChatId), bar_);
                                    }
                                }

                                saveOptions(ijo, ijo.getString("chatId"));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        StateHolder.getInstance(getApplicationContext()).setDialogSynced();
                        if (evt.getListener() != null) evt.getListener().onResponse(jo);
                    }

                    @Override
                    public void onError(Exception e, String s) {
                        if (evt.getListener() != null) evt.getListener().onError(e);
                    }
                }
        );
    }

    private void sendMediaFile(long cidd, String path, String type, final String chatId, final String name, final String description, boolean isSave) {
//        Tool.log("+++ cid = " + cidd + ", path = " + path + ", type = " + type + ", chatId = " + chatId + ", name = " + name + ", desc = " + description + ", isSave = " + isSave);

        Storage storage = Storage.getInstance(this);
        long cid = cidd;
        final byte[] previewBytes;
        String length;
        boolean pathIsURI = false;
        InputStream isf;
        Bitmap bmp = null;
        try {
            Uri i = Uri.parse(path);
            isf = getContentResolver().openInputStream(i);
            pathIsURI = true;
        } catch (Exception e) {
            try {
                isf = new FileInputStream(path);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                return;
            }
        }

        if (path.endsWith(".mp4") || path.endsWith(".mp3")) {
            MediaPlayer mp = MediaPlayer.create(this, Uri.parse(path));
//                MediaPlayer mp = new MediaPlayer();
//                try {
//                    mp.setDataSource(this, Uri.parse(path));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            length = String.valueOf(mp.getDuration() / 1000);
            mp.release();
        } else {
            try {
                length = isf == null ? "0" : String.valueOf(isf.available() / 1024);
            } catch (IOException e) {
                length = null;
            }
        }
        try {
            if (pathIsURI) {
                switch (type) {
                    case "image/*":
                    case "image/png":
                    case "image/jpeg":
                        previewBytes = Tool.extractThumbnail(this, path, 200, 150);

                        if (isSave) cid = storage.saveNewImgMessage(path, previewBytes, chatId);

                        bmp = Tool.extractThumbnail1(this, path, 1024, 768);
                        byte[] urlBytes = Tool.extractThumbnail2(bmp);

                        isf = new ByteArrayInputStream(urlBytes);
                        break;
                    case "image/gif":
                        GifDecoder gifDecoder = new GifDecoder();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = isf.read(buffer)) > -1) {
                            baos.write(buffer, 0, len);
                        }
                        baos.flush();

                        InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
                        isf = new ByteArrayInputStream(baos.toByteArray());

                        gifDecoder.read(Tool.stream2Bytes(is1));
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        gifDecoder.advance();
                        gifDecoder.getNextFrame().compress(Bitmap.CompressFormat.PNG, 100, stream);
                        previewBytes = stream.toByteArray();
                        if (isSave) cid = storage.saveNewImgMessage(path, previewBytes, chatId);
                        break;
                    default:
                        previewBytes = null;
                        if (isSave)
                            cid = storage.saveNewFileMessage(path, name, length, type, chatId);
                }
                if (MimeTypeMap.getSingleton().hasMimeType(type))
                    path = "file." + MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
            } else {
                if (path.endsWith(".mp4")) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND).compress(Bitmap.CompressFormat.PNG, 100, stream);
                    previewBytes = stream.toByteArray();
                    // TODO: save
                    cid = -1;
                } else if (path.endsWith(".gif")) {
                    GifDecoder gifDecoder = new GifDecoder();
                    gifDecoder.read(Tool.stream2Bytes(isf));
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    gifDecoder.advance();
                    gifDecoder.getNextFrame().compress(Bitmap.CompressFormat.PNG, 100, stream);
                    previewBytes = stream.toByteArray();
                    if (isSave) cid = storage.saveNewImgMessage(path, previewBytes, chatId);
                } else if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                    previewBytes = Tool.extractThumbnail(this, path, 200, 150);
                    if (isSave) cid = storage.saveNewImgMessage(path, previewBytes, chatId);
                } else if (path.endsWith(".mp3")) {
                    previewBytes = null;
                    if (isSave)
                        cid = storage.saveNewMp3Message(path, name, description, type, chatId);
                } else {
                    previewBytes = null;
                    if (isSave)
                        cid = storage.saveNewFileMessage(path, path, length, path.substring(path.lastIndexOf(".")), chatId);
                }
            }

            storage.setChatStatus(chatId, ChatBased.STATUS_STR_NONE);
            Bus.getInstance().post(new MsgUpdatedEvent(chatId));

            if (pathIsURI && (type.contains("png") || type.contains("jpeg") || type.contains("jpg"))) {
                final long finalCid1 = cid;
                chat.sendFile2Chat(storage.getSID(), isf, previewBytes, bmp == null ? 1024 : bmp.getWidth(), bmp == null ? 768 : bmp.getHeight(), path, false, description, length, chatId, null, null, new ChatFacade.SendFileListener() {
                    @Override
                    public void onSuccess(String serverId, long time, String className, String type, String url) {
                        switch (className) {
                            case ChatFacade.CLASS_IMAGE_ROUTE:
                                Storage.getInstance(Sender.this).setImgMessageSent(finalCid1, url, previewBytes, serverId, time, type);
                                Bus.getInstance().post(new MsgUpdatedEvent(chatId));
                                break;
                        }
                    }

                    @Override
                    public void onError(Exception e, String req) {
                        e.printStackTrace();
                    }
                });
            } else {
                final long finalCid = cid;

                chat.sendFile2Chat(storage.getSID(), isf, previewBytes, path, false, description, length, chatId, null, null, new ChatFacade.SendFileListener() {
                    @Override
                    public void onSuccess(String serverId, long time, String className, String type, String url) {
                        switch (className) {
                            case ChatFacade.CLASS_IMAGE_ROUTE:
                                Storage.getInstance(Sender.this).setImgMessageSent(finalCid, url, previewBytes, serverId, time, type);
                                Bus.getInstance().post(new MsgUpdatedEvent(chatId));
                                break;
                            case ChatFacade.CLASS_AUDIO_ROUTE:
                                Storage.getInstance(Sender.this).setAudioMessageSent(finalCid, url, name, description, type, serverId, String.valueOf(time));
                                Bus.getInstance().post(new MsgUpdatedEvent(chatId));
                                break;
                        }
                    }

                    @Override
                    public void onError(Exception e, String req) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Bus.getInstance().post(new MsgUpdatedEvent(chatId));
    }


    private interface ChatInfoListener {
        void onInfoFinished();
    }

    private void fullStop() {
        if (chat != null) {
            chat.stop();
        }
        chat = null;
        onDestroy();
        stopSelf();
    }

    private void makeForceOpen(String chatId) {
        if (mChatId != null && !mChatId.equals(chatId)) {
            Intent i = new Intent(this, ChatActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(ChatActivity.EXTRA_CHAT_ID, chatId);
            startActivity(i);
        }
    }

    private void getUserInfo(String userId, final SyncEvent.SRespListener listener) {
        if (chat != null) {
            chat.getUserInfo(userId, new ChatFacade.JsonRespListener() {
                @Override
                public void onSuccess(JSONObject model) {
                    try {
                        User u = new User(model.getJSONObject("ct"));
                        Storage.getInstance(Sender.this).saveUser(u);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (listener != null) listener.onResponse(model);
                }

                @Override
                public void onError(Exception e, String req) {
                    if (listener != null) listener.onError(e);
                }
            });
        }
    }
}
