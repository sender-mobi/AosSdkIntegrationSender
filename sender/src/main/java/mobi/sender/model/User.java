package mobi.sender.model;

import org.json.JSONObject;

/**
 * Created by vp on 14.07.16.
 */
public class User extends ChatBased{

    private String phone, localId, mail, msgKey, btcAddr;
    private boolean isCompany/*, isOwn*/;
    public static final String P2P_CHAT_PREFIX = "user+";

    public User() {}

    public User(JSONObject jo) {
        super(jo);
        msgKey = jo.has("encryptionKey") ? jo.optString("encryptionKey") : jo.optString("msgKey", "");
        btcAddr = jo.optString("btcAddr", "");
        localId = jo.optString("localId");
        phone = jo.optString("phone");
        if (!phone.startsWith("+")) phone = "+" + phone;
        if (jo.has("isCompany")) isCompany = jo.optBoolean("isCompany");
        else {
            isCompany = "company".equals(jo.optString("type"));
        }

//        if(jo.has("isOwn") && jo.optBoolean("isOwn")){
//            isOwn = jo.optBoolean("isOwn");
//        }
    }

    public String getMsgKey() {
        return msgKey;
    }

    public String getBtcAddr() {
        return btcAddr;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public void setBtcAddr(String btcAddr) {
        this.btcAddr = btcAddr;
    }

    public String getUserId() {
        if (!super.chatId.startsWith(P2P_CHAT_PREFIX)) return super.chatId;
        return super.chatId.substring(P2P_CHAT_PREFIX.length());
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone != null && phone.length() > 3 && !phone.startsWith("+")) phone = "+" + phone;
        this.phone = phone;
    }

    public void setRawPhone(String phone) {
        this.phone = phone;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public boolean isCompany() {
        return isCompany;
    }

    public void setCompany(boolean company) {
        isCompany = company;
    }

//    public boolean isOwn() {
//        return isOwn;
//    }
//
//    public void setOwn(boolean own) {
//        isOwn = own;
//    }
}
