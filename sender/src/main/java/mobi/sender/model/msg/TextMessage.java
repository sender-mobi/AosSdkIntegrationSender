package mobi.sender.model.msg;

import com.sender.library.ChatFacade;

import org.json.JSONException;
import org.json.JSONObject;

import mobi.sender.tool.Tool;

/**
 * Created by Smmarat on 25.07.16.
 */
public class TextMessage extends MsgBased{

    private String text, pkey;
    private boolean encrypted = false, decrypted = false;
    private byte [] mByteArr = null;

    public TextMessage(String text, String chatId) {
        super(chatId);
        this.text = text;
        try {
            JSONObject jo = new JSONObject().put("text", text);
            super.model = jo.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.className = ChatFacade.CLASS_TEXT_ROUTE;
        this.packetId = System.currentTimeMillis();
        this.created = System.currentTimeMillis();
    }

    public TextMessage(String text, String chatId, long packetId) {
        super(chatId);
        this.text = text;
        try {
            JSONObject jo = new JSONObject().put("text", text);
            super.model = jo.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.className = ChatFacade.CLASS_TEXT_ROUTE;
        this.packetId = packetId;
        this.created = System.currentTimeMillis();
    }

    public TextMessage(String text, String chatId, boolean encrypted, String pKey, byte [] arrByte){
        super(chatId);
        this.text = text;
        try {
            JSONObject jo = new JSONObject().put("text", text).put("encrypted", encrypted);
            super.model = jo.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.encrypted = encrypted;
        this.pkey = pKey;
        this.className = ChatFacade.CLASS_TEXT_ROUTE;
        this.packetId = System.currentTimeMillis();
        this.created = System.currentTimeMillis();
        this.mByteArr = arrByte;
    }

    public boolean isDecrypted() {
        return decrypted;
    }

    public void setDecrypted(boolean decrypted) {
        this.decrypted = decrypted;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        try {
            JSONObject jo = new JSONObject(super.model);
            jo.put("encrypted", encrypted);
            super.model = jo.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
        try {
            JSONObject jo = new JSONObject(super.model);
            jo.put("pkey", pkey);
            super.model = jo.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        try {
            JSONObject jo = new JSONObject(super.model);
            jo.put("text", text);
            super.model = jo.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public byte[] getByteArr() {
        return mByteArr;
    }
}
