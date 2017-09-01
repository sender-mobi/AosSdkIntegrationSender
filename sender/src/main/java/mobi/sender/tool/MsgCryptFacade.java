package mobi.sender.tool;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import mobi.sender.R;
import mobi.sender.model.User;
import mobi.sender.model.msg.TextMessage;

/**
 * Created by vp
 * on 17.02.16.
 */
public class MsgCryptFacade {

    private Context ctx;
    private LWallet wallet;

    public MsgCryptFacade(Context ctx) {
        this.ctx = ctx;
        wallet = LWallet.getInstance(ctx);
    }

    public JSONObject initEncryptGroupChat(String chatId) {
        JSONObject keyPool = new JSONObject();
        try {
            Storage storage = Storage.getInstance(ctx);
            String chatKey = LWallet.getRandomAesKey();
            for (User user : storage.getChatMembers(chatId)) {
                String userid = user.getUserId();
                String msgKey = user.getMsgKey();
                if (msgKey.isEmpty()) continue;
                String encrypted = null;
                try {
                    encrypted = wallet.encrypt(wallet.pubKeyFromString(msgKey), chatKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (encrypted == null) continue;
                keyPool.put(userid, encrypted);
            }
            String myUid = storage.getMyUserId();
            String encrypted = wallet.encrypt(wallet.pubKeyFromString(wallet.getMyRootPubKey()), chatKey);
            keyPool.put(myUid, encrypted);
            storage.setChatEncrypted(chatId, true);
            storage.addDialogKey(chatId, chatKey);
            return keyPool;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public TextMessage tryEncrypt(TextMessage msg) {
        String msgText = msg.getText();
        Tool.log("try enCRYPT message: " + msg);
        try {
            Storage storage = Storage.getInstance(ctx);
            if (Tool.isP2PChat(msg.getChatId())) {
                String pKey = storage.getUserMsgKey(msg.getChatId());
                if (pKey == null || pKey.length() == 0) throw new Exception("empty key");
                msgText = wallet.encrypt(wallet.pubKeyFromString(pKey), msgText);
                msg.setPkey(wallet.getMyRootPubKey());
            } else {
                JSONArray keys = storage.getDialogKeys(msg.getChatId());
                if (keys.length() == 0) throw new Exception("keys.length() == 0");
                String chatKey = keys.getString(keys.length() - 1);
                msgText = LWallet.encryptAes(chatKey, msgText);
            }
            msg.setText(msgText);
            msg.setEncrypted(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    public TextMessage tryDecrypt(TextMessage msg) {
        if (msg.isEncrypted() && !msg.isDecrypted()) {
            Storage storage = Storage.getInstance(ctx);
            try {
                if (Tool.isP2PChat(msg.getChatId())) {
                    boolean fromMe = storage.getMyUserId().equals(msg.getFrom());
                    String key;
                    if (fromMe) {
                        key = storage.getUserMsgKey(msg.getChatId());
                    } else {
                        key = msg.getPkey();
                    }
                    if (key == null || key.length() == 0) throw new Exception("empty key");
                    String text = "";
                    if(msg.getByteArr() == null) {
                        text = wallet.decrypt(wallet.pubKeyFromString(key), msg.getText());
                    }else{
                        text = wallet.decrypt(wallet.pubKeyFromString(key), msg.getByteArr());
                    }
                    if (text == null || text.equalsIgnoreCase(msg.getText()))
                        throw new Exception("not decoded");
                    msg.setText(text);
                } else {
                    JSONArray keys = storage.getDialogKeys(msg.getChatId());
                    if (keys.length() == 0) throw new Exception("keys.length() == 0");
                    String msgText = null;
                    for (int i = keys.length() - 1; i >= 0; i--) {
                        String key = keys.getString(i);
                        try {
                            msgText = LWallet.decryptAes(key, msg.getText());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (msgText != null && !msgText.equalsIgnoreCase(msg.getText()))
                            break;
                    }
                    if (msgText == null || msgText.equalsIgnoreCase(msg.getText()))
                        throw new Exception("not decoded");
                    msg.setText(msgText);
                }
                msg.setDecrypted(true);
            } catch (Exception e) {
                e.printStackTrace();
                msg.setText(ctx.getString(R.string.msg_encr_mess));
            }
        }
        return msg;
    }
}
