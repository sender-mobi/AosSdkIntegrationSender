package mobi.sender.tool;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.Add2ChatReq;
import mobi.sender.event.AlertReq;
import mobi.sender.event.GetCompanyOperatorsReq;
import mobi.sender.event.LeaveChatReq;
import mobi.sender.event.SendFileReq;
import mobi.sender.event.SendFormReq;
import mobi.sender.event.SendGAuthTokenReq;
import mobi.sender.event.SendLocationReq;
import mobi.sender.event.SendStickerReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.event.UploadFileReq;
import mobi.sender.model.ChatBased;
import mobi.sender.model.User;
import mobi.sender.model.msg.FormMessage;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.tool.utils.MediaUtils;
import mobi.sender.ui.BaseActivity;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.MainActivity;
import mobi.sender.ui.SettingsActivity;
import mobi.sender.ui.window.AddOneUserWindow;
import mobi.sender.ui.window.AddSeveralUserWindow;
import mobi.sender.ui.window.FileChooseWindow;
import mobi.sender.ui.window.map.MapWindow;

public class ActionExecutor {

    public static final String OPERATION_RESIGN = "reCryptKey";
    public static final String OPERATION_SEND_GOOGLE_2FA = "setGoogleToken";
    public static final String OPERATION_GOTO = "goTo";
    public static final String OPERATION_SEND_MEDIA = "sendMedia";
    public static final String OPERATION_SUBMIT_CHANGE = "submitOnChange";
    public static final String OPERATION_ADD_USER = "addUser";
    public static final String TYPE_STICKER = "sticker";
    public static final String TYPE_VOICE = "voice";
    public static final String TYPE_LOCATION = "location";
    public static final String TYPE_PHOTO = "photo";
    public static final String TYPE_FILE = "file";
    public static final String TYPE_VIDEO = "video";
    public static final String OPERATION_QR_SHOW = "showAsQr";
    public static final String OPERATION_QR_SCAN = "qrScan";
    public static final String OPERATION_SCAN_QR_TO = "scanQrTo";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_STICKER_ID = "stickerId";
    public static final String PARAM_FILE_PATH = "filePath";
    public static final String PARAM_VIEW = "view";
    public static final String PARAM_LENGTH = "length";
    public static final String OPERATION_SWITCH_CRYPTO = "switchCrypto";
    public static final String TELEPHONE_PREFIX = "tel:+";
    public static final String PARAM_USER_ID = "userId";
    public static final String PARAM_CARD = "card";
    public static final String PARAM_URL = "url";
    public static final String OPERATION_CALL_ROBOT = "callRobot";
    public static final String PARAM_CLASS = "class";
    public static final String PARAM_PHONE = "phone";
    public static final String OPERATION_VIEW = "viewLink";
    public static final String OPERATION_CALL_PHONE = "callPhone";
    public static final String OPERATION_SCAN_CARD = "scanCard";
    public static final String OPERATION_SELECT_USER = "selectUser";
    public static final String OPERATION_START_P2P_CHAT = "startP2PChat";
    public static final String OPERATION_CALL_ROBOT_IN_P2P_CHAT = "callRobotInP2PChat";
    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_REG = "reg";
    public static final String RESULT_TRUE = "true";
    public static final String PARAM_TO = "to";
    public static final String PARAM_TO_NAME = "toName";
    public static final String PARAM_TO_AMT = "to_amt";
    public static final String TAG_VIBRO = "_vibro";
    public static final String PARAM_FROM_MAIN_ACTIVITY = "fromMainActivity";
    public static final String TYPE_LEAVE_CHAT = "leaveChat";
    public static final String TYPE_TWITCH = "twitch";
    private static final String TAG = ActionExecutor.class.getSimpleName();
    private static final int REQUEST_SCAN_QR = 1009;
    private static final String TYPE_VIBRO = "vibro";
    private static final String PARAM_LINK = "link";
    private static final String OPERATION_CHOOSE_FILE = "chooseFile";
    private static final String PARAM_RESULT = "result";
    private static final String PARAM_AUTOSUBMIT = "autosubmit";
    private static final String OPERATION_NOTARIZE = "notarizeFile";
    private static final String OPERATION_SIGN = "signFile";
    private static final String OPERATION_SHOW_NOTAS = "showBtcNotas";
    private static final String OPERATION_SHOW_ARHIVE = "showBtcArhive";
    private static final String OPERATION_SHARE = "share";
    private static final String OPERATION_COPY = "copy";
    private static final String PARAM_FROM = "from";
    private static final String PARAM_ADDR = "addr";
    private static final String PARAM_SUMM = "summ";
    private static final String OPERATION_SEND_BTC = "sendBtc";
    private static final String OPERATION_GET_BTC = "getBtc";
    private static final String OPERATION_SEND_CAMERA_PHOTO = "sendCameraPhoto";
    private static final String OPERATION_SUPPORT = "support";

    private final BaseActivity act;
    private String mChatId;
    private OnActionFinished mOnActionFinished;
    private GAuthHelper gah;

    public ActionExecutor(BaseActivity activity, String chatId) {
        act = activity;
        this.mChatId = chatId;
        gah = new GAuthHelper(activity);
    }

    public void exec(String oper, Map<String, Object> params, String filterText) {
        JSONObject data = new JSONObject();
        if (filterText != null && !filterText.isEmpty())
            try {
                data.put("text", filterText);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        else data = null;
        exec(data, oper, params);
    }

    public void exec(JSONObject mData, String oper, final Map<String, Object> params) {
        final JSONObject data = mData == null ? new JSONObject() : mData;
        if (OPERATION_SEND_MEDIA.equalsIgnoreCase(oper)) {
            String type = params.get(PARAM_TYPE).toString();
            if (TYPE_VOICE.equalsIgnoreCase(type)) {
                sendVoice(params.get(PARAM_FILE_PATH).toString(), params.get(PARAM_LENGTH).toString());
            } else if (TYPE_LOCATION.equalsIgnoreCase(type)) {
                piclLocation();
            } else if (TYPE_PHOTO.equalsIgnoreCase(type)) {
                getPhoto();
            } else if (TYPE_FILE.equalsIgnoreCase(type)) {
                getFile();
            } else if (TYPE_VIDEO.equalsIgnoreCase(type)) {
                getVideo();
            } else if (TYPE_VIBRO.equalsIgnoreCase(type)) {
                sendVibro();
            } else if (TYPE_TWITCH.equalsIgnoreCase(type)) {
                Bus.getInstance().post(new AlertReq(mChatId));
            } else if (TYPE_STICKER.equalsIgnoreCase(type)) {
                postSticker(params.get("stickerId").toString());
            } else if (TYPE_VIBRO.equalsIgnoreCase(type)) {
                sendVibro();
            }
        } else if (OPERATION_GOTO.equalsIgnoreCase(oper)) {
            if (!params.keySet().contains(PARAM_TO) || !params.keySet().contains(PARAM_MESSAGE)) {
                Log.d(TAG, "required params not found");
                return;
            }
            String to = params.get(PARAM_TO).toString();
            Intent i = null;
            if ("contacts".equalsIgnoreCase(to)) {
                i = new Intent(act, MainActivity.class);
            } else if ("dialogs".equalsIgnoreCase(to)) {
                i = new Intent(act, MainActivity.class);
            } else if ("profile".equalsIgnoreCase(to)) {
                i = new Intent(act, MainActivity.class);
            } else if ("settings".equalsIgnoreCase(to)) {
                i = new Intent(act, SettingsActivity.class);
            } else if ("contact".equalsIgnoreCase(to)) {
                if (mChatId.startsWith(User.P2P_CHAT_PREFIX)) {
                    String userId = mChatId.substring(User.P2P_CHAT_PREFIX.length());
                    // TODO: implement
//                    i = new Intent(act, AcContactInfo.class);
//                    i.putExtra(AcContactInfo.EXTRA_USER_ID, userId);
                }
            }
            if (i != null) {
                act.startActivity(i);
                tryFinish(params);
            }
        } else if (OPERATION_SUBMIT_CHANGE.equalsIgnoreCase(oper)) {
            FormMessage fm = (FormMessage) params.get(PARAM_MESSAGE);
            Bus.getInstance().post(new SendFormReq(fm.getClassName(), fm.getChatId(), fm.getProcId(), data));
            returnAction();
        } else if (OPERATION_RESIGN.equalsIgnoreCase(oper)) {
            resign(params, data);
        } else if (OPERATION_SEND_GOOGLE_2FA.equalsIgnoreCase(oper)) {
            sendGoogle2FA(params);
        } else if (OPERATION_QR_SHOW.equalsIgnoreCase(oper)) {
            showQrWindow(params);
        } else if (OPERATION_QR_SCAN.equalsIgnoreCase(oper)) {
            scanQR();
        } else if (OPERATION_SCAN_QR_TO.equalsIgnoreCase(oper)) {
            scanQR(params, data);
        } else if (OPERATION_CHOOSE_FILE.equalsIgnoreCase(oper)) {
            chooseFile(params, data);
        } else if (OPERATION_SEND_CAMERA_PHOTO.equalsIgnoreCase(oper)) {
            sendCameraPhoto(params, data);
        } else if (OPERATION_NOTARIZE.equalsIgnoreCase(oper)) {
            notarizeFile(params, data);
        } else if (OPERATION_SHOW_NOTAS.equalsIgnoreCase(oper)) {
            showNotarizations(params, data);
        } else if (OPERATION_SHOW_ARHIVE.equalsIgnoreCase(oper)) {
            showBtcArhive(params, data);
        } else if (OPERATION_SHARE.equalsIgnoreCase(oper)) {
            shareBtcAddr(params);
        } else if (OPERATION_COPY.equalsIgnoreCase(oper)) {
            copyBtcAddr(params);
        } else if (OPERATION_SIGN.equalsIgnoreCase(oper)) {
            signFile(params, data);
        } else if (OPERATION_SEND_BTC.equalsIgnoreCase(oper)) {
            sendBtc(params, data);
        } else if (OPERATION_GET_BTC.equalsIgnoreCase(oper)) {
            getBtc(params, data);
        } else if (OPERATION_CALL_ROBOT.equalsIgnoreCase(oper)) {
            callRobot(params, data);
        } else if (OPERATION_SWITCH_CRYPTO.equalsIgnoreCase(oper)) {
            // TODO: implement
//            Set<String> set = pref.getStringSet(App.PROP_ENCRYPTED_CHATS, new HashSet<String>());
//            boolean enable = !set.contains(mChatId);
//            if (enable) {
//                set.add(mChatId);
//                App.log("senderKey: chat " + mChatId + " set encrypted by key");
//            } else {
//                set.remove(mChatId);
//                App.log("senderKey: chat " + mChatId + " set decrypted by key");
//            }
//            pref.edit().putStringSet(App.PROP_ENCRYPTED_CHATS, set).apply();
//            if (!mChatId.startsWith(App.P2P_CHAT_PREFIX)) {
//                new MsgCryptFacade(act).initEncryptGroupChat(mChatId, chatMembers, enable);
//            }
            if (Tool.isP2PChat(mChatId)) {

            } else {

            }
        } else if (OPERATION_SCAN_CARD.equalsIgnoreCase(oper)) {
            showCardScanWindow(params, data);
        } else if (OPERATION_CALL_PHONE.equalsIgnoreCase(oper)) {
            callPhone(params);
        } else if (OPERATION_SELECT_USER.equalsIgnoreCase(oper)) {
            showSelectUserWindow(params, data);
        } else if (OPERATION_START_P2P_CHAT.equalsIgnoreCase(oper)) {
            startP2pChat(params);
        } else if (OPERATION_CALL_ROBOT_IN_P2P_CHAT.equalsIgnoreCase(oper)) {
            callRobotInP2P(params, data);
        } else if (OPERATION_VIEW.equalsIgnoreCase(oper)) {
            viewAction(params);
        } else if (OPERATION_SUPPORT.equalsIgnoreCase(oper)) {
            String type = params.get(PARAM_TYPE).toString();
            if (TYPE_LEAVE_CHAT.equalsIgnoreCase(type)) {
                leaveChatAction();
            }
        } else if (OPERATION_ADD_USER.equalsIgnoreCase(oper)) {
            Storage storage = Storage.getInstance(act);

            if (storage.isOperChat(mChatId)) {
                mobi.sender.model.Dialog dialog = (mobi.sender.model.Dialog) storage.getChat(mChatId);
                Bus.getInstance().post(new GetCompanyOperatorsReq(dialog.getCompanyId(), new SyncEvent.SRespListener() {
                    @Override
                    public void onResponse(JSONObject data) {
                        final List<User> list = new ArrayList<>();
                        Storage storage = Storage.getInstance(act);
                        try {
                            JSONArray arr = data.getJSONArray("operators");
                            List<String> idsMembers = new ArrayList<>();
                            for (User u : storage.getChatMembers(mChatId)) {
                                idsMembers.add(u.getUserId());
                            }
                            for (int i = 0; i < arr.length(); i++) {
                                User user = new User(arr.getJSONObject(i));
                                if (storage.getMyUserId().equals(user.getUserId())) continue;
                                if (idsMembers.contains(user.getUserId())) continue;
                                list.add(user);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        makeAddSeveralUserDialog(list);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }));
            } else {
                List<User> list = Storage.getInstance(act).getAddCandidates(mChatId);
                makeAddSeveralUserDialog(list);
            }
        }
    }

    private void makeAddSeveralUserDialog(final List<User> list) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AddSeveralUserWindow(act, list, new AddSeveralUserWindow.DoneListener() {
                    @Override
                    public void onDonePressed(List<String> chekedList, final Dialog dialog) {
                        JSONArray ja = new JSONArray();
                        for (String userId : chekedList) {
                            ja.put(userId);
                        }

                        Bus.getInstance().post(new Add2ChatReq(mChatId, ja, new SyncEvent.SRespListener() {
                            @Override
                            public void onResponse(JSONObject data) {
                                dialog.dismiss();
                                act.startActivity(new Intent(act, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, data.optString("chatId")));
                            }

                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                            }
                        }));
                    }
                }).show();
            }
        });
    }

    private void leaveChatAction() {
        Bus.getInstance().post(new LeaveChatReq(mChatId));
    }

    private void sendCameraPhoto(final Map<String, Object> params, final JSONObject data) {
        // TODO: implement
//        Intent intent = new Intent(act, AcCamera.class);
//        act.startActivityWithCallback(intent, AcChat.ACTION_SEND_CAMERA_PHOTO, new OnActivityResultCallback() {
//            @Override
//            public void call(Intent result) {
//                if (result != null) {
//                    try {
//                        String filePath = result.getStringExtra(AcCamera.EXTRA_URI);
//                        App.log("fml load photo = " + filePath);
//                        final ProgressDialog progressDialog = new ProgressDialog(act);
//                        progressDialog.setMessage(act.getString(R.string.loading));
//                        progressDialog.setCancelable(false);
//                        progressDialog.show();
//                        Bus.getInstance().register(new Bus.Subscriber() {
//                            @Override
//                            public void onEvent(Bus.Event event) {
//                                try {
//                                    progressDialog.dismiss();
//                                    String url = ((UploadImageResponse) event).getUrl();
//                                    if (url != null && !url.isEmpty()) {
//                                        App.log("camera image url: " + url);
//                                        data.put(((UploadImageResponse) event).getParamName(), url);
//                                        App.log("camera pre data: " + data);
//                                    }
//                                    if (mOnActionFinished != null)
//                                        mOnActionFinished.refreshForm();
//                                } catch (Exception e) {
//                                    App.track(e);
//                                } finally {
//                                    Bus.getInstance().unregister(this);
//                                }
//                            }
//                        }, UploadImageResponse.class.getSimpleName());
//                        Bus.getInstance().post(new UploadImageRequest(filePath, 85, params.get(PARAM_TO).toString()));
//                    } catch (Exception e) {
//                        App.track(e);
//                    }
//                }
//            }
//        });
    }

    private void chooseFile(final Map<String, Object> params, final JSONObject jo) {
        new FileChooseWindow(act, new FileChooseWindow.OnFileSelectedListener() {
            @Override
            public void onFileSelected(File file) {
                try {
                    Tool.log("fml load file = " + file.getAbsolutePath());
                    final ProgressDialog progressDialog = new ProgressDialog(act);
                    progressDialog.setMessage("Wait");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    Bus.getInstance().post(new UploadFileReq(file.getAbsolutePath(), new SyncEvent.SRespListener() {
                        @Override
                        public void onResponse(JSONObject data) {
                            try {
                                progressDialog.dismiss();
                                String url = data.optString("url");
                                Tool.log("load file url: " + url);
                                if (params.keySet().contains(PARAM_VIEW))
                                    jo.put(params.get(PARAM_VIEW).toString(), url);
                                if (params.keySet().contains(PARAM_FILE_PATH))
                                    jo.put(params.get(PARAM_FILE_PATH).toString(), url);
                                if (params.keySet().contains(PARAM_TO))
                                    jo.put(params.get(PARAM_TO).toString(), url);
                                Tool.log("load file data: " + data);
                                if (mOnActionFinished != null) {
                                    mOnActionFinished.refreshForm();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                        }
                    }));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel() {

            }
        }).show();
    }

    private void sendGoogle2FA(Map<String, Object> params) {
        final String[] accn = gah.getAccNames();
        if (accn == null) return;
        if (accn.length == 0) {
            Toast.makeText(act, "Stored Google accounts not found", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(R.string.tst_select_google_account);
            builder.setItems(accn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, final int which) {
                    gah.getAuthToken(accn[which], new GAuthHelper.OAuthCallbackListener() {
                        @Override
                        public void callback(String authToken) {
                            if (authToken == null) {
                                Toast.makeText(act, R.string.tst_operation_canceled, Toast.LENGTH_LONG).show();
                            } else {
                                Bus.getInstance().post(new SendGAuthTokenReq(authToken));
                                Toast.makeText(act, R.string.tst_success, Toast.LENGTH_LONG).show();
                                returnAction();
                            }
                        }
                    });
                }
            }).create().show();
        }
    }

    private void notarizeFile(final Map<String, Object> params, final JSONObject data) {
        // TODO: implement
//        try {
//            data.put(PARAM_RESULT, "Ошибка");
//            if (!Tool.isEmptyString(data.getString(params.get(PARAM_FROM).toString()))) {
//                final String path = data.getString(params.get(PARAM_FROM).toString());
//                BtcFasade.notarize(act, path, new BtcFasade.PayListener() {
//                    @Override
//                    public void onSuccess(String txHash) {
//                        DbHelper.getInstance().saveNotarization(new Notarization(path, txHash));
//                        try {
//                            data.put(PARAM_RESULT, "Документ нотаризован, id bitcoin-транзакции: " + txHash);
//                            postFormWithMessage(params, data);
//                            returnAction();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        App.track(e);
//                    }
//                });
//            }
//        } catch (Exception e) {
//            App.track(e);
//        }
    }

    private void showNotarizations(final Map<String, Object> params, final JSONObject data) {
        // TODO: implement
//        List<Notarization> notarizations = DbHelper.getInstance().getNotarizations();
//        if (notarizations.size() == 0) {
//            Toast.makeText(act, R.string.notarization_achieve_null, Toast.LENGTH_LONG).show();
//            return;
//        }
//        new NotaListWindow(act, notarizations).show();
    }

    private void showBtcArhive(final Map<String, Object> params, final JSONObject data) {
        // TODO: implement
//        try {
//            List<BtcTransaction> transactions = new ArrayList<>();
//            // TODO: bitcoin history
//            if (transactions.size() == 0) {
//                Toast.makeText(act, R.string.payments_achieve_null, Toast.LENGTH_LONG).show();
//                return;
//            }
//            new BtcTransListWindow(act, transactions).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void signFile(final Map<String, Object> params, final JSONObject data) {
        // TODO: implement
//        try {
//            data.put(PARAM_RESULT, "Ошибка");
//            if (!Tool.isEmptyString(data.getString(params.get(PARAM_FROM).toString()))) {
//                final String code = data.getString(params.get(PARAM_FROM).toString());
//                LWallet wallet = LWallet.getInstance(act);
//                if (wallet == null) return;
//                final String sign = wallet.sign(code);
//                final String addr = wallet.currentReceiveAddress().toString();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            String url = new String(Base64.decode(code, Base64.DEFAULT));
//                            String req = url + "&code=" + URLEncoder.encode(code) + "&sign=" + URLEncoder.encode(sign) + "&addr=" + URLEncoder.encode(addr);
//                            App.log(req);
//                            Tool.httpGet(req);
//                            data.put(PARAM_RESULT, "Подпись отправлена для верификации");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        postFormWithMessage(params, data);
//                        returnAction();
//                    }
//                }).start();
//            }
//        } catch (Exception e) {
//            App.track(e);
//        }

    }

    private void getBtc(Map<String, Object> params, JSONObject data) {
        // TODO: implement
//        try {
//            LWallet wallet = LWallet.getInstance(act);
//            if (wallet == null) return;
//            data.put(params.get(PARAM_TO).toString(), wallet.currentReceiveAddress().toString());
//        } catch (Exception e) {
//            App.track(e);
//        }
//        postFormWithMessage(params, data);
//        App.log("get btc, data: " + data + "\nparams: " + params);
//        returnAction();
    }

    private void sendBtc(final Map<String, Object> params, final JSONObject data) {
        // TODO: implement
//        try {
//            data.put(PARAM_RESULT, "Ошибка");
//            String addr = null;
//            if (data.has(PARAM_ADDR)) addr = data.optString(PARAM_ADDR);
//            else if (params.keySet().contains(PARAM_ADDR)) addr = params.get(PARAM_ADDR).toString();
//            String summ = null;
//            if (data.has(PARAM_SUMM)) summ = data.optString(PARAM_SUMM);
//            else if (params.keySet().contains(PARAM_SUMM)) summ = params.get(PARAM_SUMM).toString();
//            if (addr == null) throw new Exception("invalid data");
//            if (addr.startsWith("+")) addr = DbHelper.getInstance().getBtcAddrByPhone(addr);
//            if (!Tool.isEmptyString(addr) && !Tool.isEmptyString(summ)) {
//                BtcFasade.pay(act, addr, Coin.parseCoin(summ), new BtcFasade.PayListener() {
//                    @Override
//                    public void onSuccess(String txHash) {
//                        try {
//                            data.put(PARAM_RESULT, "Платёж отправлен успешно");
//                            postFormWithMessage(params, data);
//                            returnAction();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        try {
//                            data.put(PARAM_RESULT, "Ошибка при отправке платежа");
//                            postFormWithMessage(params, data);
//                            returnAction();
//                        } catch (JSONException e1) {
//                            e1.printStackTrace();
//                        }
//                    }
//                });
//            }
//        } catch (Exception e) {
//            App.track(e);
//        }
    }

    private void viewAction(Map<String, Object> params) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse((String) params.get(PARAM_LINK)));
        Intent chooser = Intent.createChooser(intent, "Choose application to view");
        act.startActivity(chooser);
    }

    private void resign(Map<String, Object> params, JSONObject data) {
        LWallet wallet = LWallet.getInstance(act);
        try {
            String key = wallet.decrypt(wallet.pubKeyFromString(wallet.getMyRootPubKey()), params.get("keyCrypted").toString());
            String cryptKey = wallet.encrypt(wallet.pubKeyFromString(params.get("pubKey").toString()), key);
            data.put(params.get(PARAM_TO).toString(), cryptKey);
            if (mOnActionFinished != null)
                mOnActionFinished.refreshForm();
            postFormWithMessage(params, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendVibro() {
        // TODO: implement
        Toast.makeText(act, "В разработке", Toast.LENGTH_SHORT).show();
//        final FMessage sForm = FMessage.createStickerFMessage(TAG_VIBRO, mChatId);
//        DbHelper.getInstance().setFMessage(sForm);
//        Bus.getInstance().post(new MsgUpdatedEvent(null, sForm));
//        Bus.getInstance().post(new SendVibroRequest(mChatId, sForm.getId(), true));
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Bus.getInstance().post(new SendVibroRequest(mChatId, sForm.getId(), true));
//            }
//        }, 3000);
    }

    private void callRobotInP2P(Map<String, Object> params, JSONObject data) {
        String uid = String.valueOf(params.get(PARAM_USER_ID));
        if (uid == null || uid.trim().length() == 0) {
            Toast.makeText(act, "Invalid action", Toast.LENGTH_LONG).show();
            return;
        }
        Bus.getInstance().post(new SendFormReq(params.get(PARAM_CLASS) + "", User.P2P_CHAT_PREFIX + uid, null, data));

        Intent i = new Intent(act, ChatActivity.class);
        i.putExtra(ChatActivity.EXTRA_CHAT_ID, mChatId/*User.P2P_CHAT_PREFIX + uid*/);
        act.startActivity(i);
        tryFinish(params);
        returnAction();
    }

    private void startP2pChat(Map<String, Object> params) {
        String uid = String.valueOf(params.get(PARAM_USER_ID));
        if (uid == null || uid.trim().length() == 0) {
            Toast.makeText(act, "Invalid action", Toast.LENGTH_LONG).show();
            return;
        }
        String extraChatId = User.P2P_CHAT_PREFIX + uid;
        if (mChatId != null && mChatId.equals(extraChatId)) return;
        Intent i = new Intent(act, ChatActivity.class);
        i.putExtra(ChatActivity.EXTRA_CHAT_ID, extraChatId);
        act.startActivity(i);
        tryFinish(params);
        returnAction();
    }

    private void showSelectUserWindow(final Map<String, Object> params, final JSONObject data) {
        new AddOneUserWindow(act, Storage.getInstance(act).getUsers(false), false, new AddOneUserWindow.OnSelectUserListener() {
            @Override
            public void onSelect(ChatBased chatBased, Dialog dialog) {
                User u = (User) chatBased;
                try {
                    Object t = params.get(PARAM_TO);
                    if (t != null) data.put(t.toString(), u.getPhone());
                    Object n = params.get(PARAM_TO_NAME);
                    if (n != null) data.put(n.toString(), u.getName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (params.get(PARAM_AUTOSUBMIT) != null && params.get(PARAM_AUTOSUBMIT).toString().equalsIgnoreCase(RESULT_TRUE)) {
                    postFormWithMessage(params, data);
                    returnAction();
                } else {
                    if (mOnActionFinished != null)
                        mOnActionFinished.refreshForm();
                }
                dialog.dismiss();
            }
        }).show();
    }

    private void callPhone(Map<String, Object> params) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(TELEPHONE_PREFIX + params.get(PARAM_PHONE)));
        act.startActivity(intent);
        returnAction();
    }

    private void showQrWindow(final Map<String, Object> params) {
        // TODO: implement
        String val = (String) params.get("value");
        if (val == null) return;
        try {
            ImageView iv = new ImageView(act);
            ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams((int) Tool.convertDpToPixel(300, act), (int) Tool.convertDpToPixel(300, act));
            iv.setLayoutParams(vlp);
            iv.setPadding(10, 50, 10, 50);
            LinearLayout root = new LinearLayout(act);
            root.setOrientation(LinearLayout.VERTICAL);
            root.addView(iv);
            root.setGravity(Gravity.CENTER);
            LinearLayout ll = new LinearLayout(act);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setPadding(5, 5, 5, 12);
            TextView tv = new TextView(act);
            tv.setText(val);
            tv.setTextSize(16);
            tv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            tv.setLayoutParams(lp);
            ll.addView(tv);
            LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp3.setMargins(5, 5, 5, 5);
            ll.setGravity(Gravity.CENTER);
            root.addView(ll, lp3);
            new AlertDialog.Builder(act).setView(root).create().show();
            String urlQr = String.format("https://chart.googleapis.com/chart?cht=qr&chs=%sx%s&chl=%s&chld=%s", 400, 400, val, URLEncoder.encode("L|0", "UTF-8"));
            Picasso.with(act).load(urlQr).into(iv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncWallet() {
        // TODO: implement
//        new AlertDialog.Builder(act)
//                .setTitle(R.string.title_operation_selection)
//                .setItems(new String[]{act.getString(R.string.item_export), act.getString(R.string.item_import)}, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int item) {
//                        switch (item) {
//                            case 0:
//                                showExportWindow();
//                                break;
//                            case 1:
//                                importBtcWallet();
//                                break;
//                        }
//                    }
//                }).setCancelable(false)
//                .create().show();
    }

    private void showExportWindow() {
        // TODO: implement
//        View v = LayoutInflater.from(act).inflate(R.layout.btc_export_key_window, null);
//        TextView tv = (TextView) v.findViewById(R.id.btc_ex_text);
//        ImageView iv = (ImageView) v.findViewById(R.id.btc_ex_qr);
//        try {
//            LWallet wallet = LWallet.getInstance(act);
//            if (wallet == null) return;
//            String val = wallet.exportSeed();
//            String urlQr = String.format("https://chart.googleapis.com/chart?cht=qr&chs=%sx%s&chl=%s&chld=%s", 400, 400, val, URLEncoder.encode("L|0", "UTF-8"));
//            if (!Tool.isEmptyString(urlQr)) {
//                Tool.loadImage(act, urlQr, null, iv, 0, 0);
//                tv.setText(val);
//            }
//            new AlertDialog.Builder(act).setView(v).create().show();
//        } catch (Exception e) {
//            App.track(e);
//            Toast.makeText(act, R.string.error, Toast.LENGTH_LONG).show();
//        }

    }

    private void importBtcWallet() {
        // TODO: implement
//        Intent intentScan = new Intent(act, CaptureActivity.class);
//        intentScan.setAction(ContactsContract.Intents.Scan.ACTION);
//        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
//        intentScan.putExtra(CaptureActivity.EXTRA_SCAN_MODE, CaptureActivity.SCAN_MODE);
//        intentScan.putExtra(CaptureActivity.SCAN_FORMATS, CaptureActivity.SCAN_MODE);
//        intentScan.putExtra(ContactsContract.Intents.Scan.WIDTH, (int) Tool.convertDpToPixel(300, act));
//        intentScan.putExtra(ContactsContract.Intents.Scan.HEIGHT, (int) Tool.convertDpToPixel(300, act));
//        intentScan.putExtra(App.PREF_LOCALE_T, pref.getString(App.PREF_LOCALE_T, App.PREF_LOCALE_DEFAULT_T));
//        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//        act.startActivityWithCallback(intentScan, REQUEST_SCAN_QR, new OnActivityResultCallback() {
//                    @Override
//                    public void call(final Intent result) {
//                        try {
//                            String rez = result.getStringExtra(CaptureActivity.SCAN_RESULT);
//                            LWallet wallet = LWallet.getInstance(act);
//                            if (wallet == null) return;
//                            wallet.regenerate(rez);
//                            Toast.makeText(act, R.string.import_btc_success, Toast.LENGTH_LONG).show();
//                        } catch (Exception e) {
//                            App.track(e);
//                            Toast.makeText(act, R.string.error, Toast.LENGTH_LONG).show();
//                        }
//                        if (mOnActionFinished != null)
//                            mOnActionFinished.refreshForm();
//                    }
//                }
//        );
    }

    private void shareBtcAddr(final Map<String, Object> params) {
        // TODO: implement
//        String val = (String) params.get("value");
//        if (val == null) return;
//        try {
//            if ("{{!meta.me.btc_addr}}".equals(val)) {
//                LWallet wallet = LWallet.getInstance(act);
//                if (wallet == null) return;
//                val = wallet.currentReceiveAddress().toString();
//            }
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("text/plain");
//            intent.putExtra(Intent.EXTRA_TEXT, val);
//            act.startActivity(Intent.createChooser(intent, "Share with"));
//        } catch (Exception e) {
//            App.track(e);
//        }
    }

    private void copyBtcAddr(final Map<String, Object> params) {
        // TODO: implement
//        String val = (String) params.get("value");
//        if (val == null) return;
//        try {
//            if ("{{!meta.me.btc_addr}}".equals(val)) {
//                LWallet wallet = LWallet.getInstance(act);
//                if (wallet == null) return;
//                val = wallet.currentReceiveAddress().toString();
//            }
//            ClipboardManager clipboard = (ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE);
//            clipboard.setText(val);
//            Toast.makeText(act, R.string.address_copied_to_clipboard, Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            App.track(e);
//        }
    }

    private void showCardScanWindow(final Map<String, Object> params, final JSONObject data) {
        // TODO: implement
//        new CardScanWindow(act, new CardScanWindow.OnScanListener() {
//            @Override
//            public void onScan(String card) {
//                App.log("card scanned: " + card);
//                try {
//                    data.put(PARAM_CARD, card);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                postFormWithMessage(params, data);
//                returnAction();
//            }
//
//            @Override
//            public void onCancel() {
//                act.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(act, act.getString(R.string.card_not_scanned), Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//        }).show();
    }

    private void callRobot(Map<String, Object> params, JSONObject data) {
        Bus.getInstance().post(new SendFormReq(params.get(PARAM_CLASS).toString(), mChatId, null, data));
        returnAction();
    }

    private void returnAction() {
        if (mOnActionFinished != null)
            mOnActionFinished.onActionFinished(true);
    }

    private void postSticker(String tag) {
        Bus.getInstance().post(new SendStickerReq(tag, mChatId));
    }

    private void sendVoice(String fileName, String length) {
        Bus.getInstance().post(new SendFileReq(mChatId, "", length + "", fileName, null));
    }

    private void piclLocation() {
        new MapWindow((ChatActivity) act, new MapWindow.OnSelectListener() {
            @Override
            public void onSelect(String address, double lat, double lon) {
                Bus.getInstance().post(new SendLocationReq(address + "", lat + "", lon + "", mChatId));
            }

            @Override
            public void onCancel() {
            }
        }, MapWindow.MAP_FOR_SEND).show();
    }

    private void getPhoto() {
        CharSequence[] items = new CharSequence[]{act.getString(R.string.dlg_gallery), act.getString(R.string.dlg_camera)};
        DialogUtils.itemsDialog(act, R.string.dlg_choose_image_source, items, new DialogUtils.OnChooseListener() {
            @Override
            public void onSelect(int position) {
                switch (position) {
                    case 0:
                        MediaUtils.openGallary(act);
                        break;
                    case 1:
                        MediaUtils.openCamera(act);
                        break;
                }
            }
        });
    }

    private void getVideo() {
        // TODO: implement
        Toast.makeText(act, "В разработке", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(act, AcVideo.class);
//        act.startActivityForResult(intent, AcChat.REQUEST_SEND_VIDEO);
    }

    private void getFile() {
        MediaUtils.openFileChoose(act, new MediaUtils.OnSelectListener() {
            @Override
            public void onSelect(File file) {
                ((ChatActivity) act).processFile(Uri.fromFile(file));
            }
        });
    }

    private void scanQR() {
        // TODO: implement
        new IntentIntegrator(act).initiateScan();

//        Intent intentScan = new Intent(act, CaptureActivity.class);
//        intentScan.setAction(Intents.Scan.ACTION);
//        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
//        intentScan.putExtra(CaptureActivity.EXTRA_SCAN_MODE, CaptureActivity.SCAN_MODE);
//        intentScan.putExtra(CaptureActivity.SCAN_FORMATS, CaptureActivity.SCAN_MODE);
//        intentScan.putExtra(Intents.Scan.WIDTH, (int) Tool.convertDpToPixel(300, act));
//        intentScan.putExtra(Intents.Scan.HEIGHT, (int) Tool.convertDpToPixel(300, act));
//        intentScan.putExtra(App.PREF_LOCALE_T, pref.getString(App.PREF_LOCALE_T, App.PREF_LOCALE_DEFAULT_T));
//        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//        act.startActivityForResult(intentScan, REQUEST_SCAN_QR);
    }

    private void scanQR(final Map<String, Object> params, final JSONObject data) {
//        Intent intentScan = new Intent(act, CaptureActivity.class);
//        intentScan.setAction(Intents.Scan.ACTION);
//        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
//        intentScan.putExtra(CaptureActivity.EXTRA_SCAN_MODE, CaptureActivity.SCAN_MODE);
//        intentScan.putExtra(CaptureActivity.SCAN_FORMATS, CaptureActivity.SCAN_MODE);
//        intentScan.putExtra(Intents.Scan.WIDTH, (int) Tool.convertDpToPixel(300, act));
//        intentScan.putExtra(Intents.Scan.HEIGHT, (int) Tool.convertDpToPixel(300, act));
//        intentScan.putExtra(App.PREF_LOCALE_T, pref.getString(App.PREF_LOCALE_T, App.PREF_LOCALE_DEFAULT_T));
//        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//        act.startActivityWithCallback(intentScan, REQUEST_SCAN_QR, new OnActivityResultCallback() {
//                    @Override
//                    public void call(final Intent result) {
//                        try {
//                            String rez = result.getStringExtra(CaptureActivity.SCAN_RESULT);
//                            String amt = null;
//                            if (rez.startsWith("bitcoin:")) {
//                                rez = rez.substring("bitcoin:".length());
//                                if (rez.contains("?")) {
//                                    if (rez.contains("amount=")) {
//                                        amt = rez.substring(rez.indexOf("amount=") + "amount=".length());
//                                        if (amt.contains("&")) {
//                                            amt = amt.substring(0, amt.indexOf("&"));
//                                        }
//                                    }
//                                    rez = rez.substring(0, rez.indexOf("?"));
//                                }
//                            }
//                            data.put(params.get(PARAM_TO).toString(), rez);
//                            if (amt != null) {
//                                data.put(params.get(PARAM_TO_AMT).toString(), amt);
//                            }
//                        } catch (JSONException e) {
//                            App.track(e);
//                        }
//                        if (mOnActionFinished != null)
//                            mOnActionFinished.refreshForm();
//                    }
//                }
//        );
    }

    private void postFormWithMessage(Map<String, Object> params, JSONObject data) {
        FormMessage msg = null;
        if (params.containsKey(PARAM_MESSAGE))
            msg = (FormMessage) params.get(PARAM_MESSAGE);
        if (msg != null)
            Bus.getInstance().post(new SendFormReq(msg.getClassName(), mChatId, msg.getProcId(), data));
    }

    private void tryFinish(Map<String, Object> params) {
        if (!params.containsKey(PARAM_FROM_MAIN_ACTIVITY))
            act.finish();
    }

    public void setOnActionListener(OnActionFinished listener) {
        mOnActionFinished = listener;
    }

    public interface OnActionFinished {
        void onActionFinished(boolean disableForm);

        void refreshForm();
    }

    public interface OnActivityResultCallback {
        void call(Intent result);
    }
}
