package mobi.sender.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mobi.sender.tool.Tool;

/**
 * Created by vp on 15.07.16.
 */
public class ChatBased {

    protected long messageTime;
    protected String messageText, chatPhoto, name, chatId;
    protected boolean isBlocked, isFavorite, isMute = false;
    protected int countUnread;
    public static final int STATUS_NONE = -1;
    public static final int STATUS_DELIV = 2;
    public static final int STATUS_READ = 3;
    public static final String STATUS_STR_NONE = "none";
    public static final String STATUS_STR_DELIV = "deliv";
    public static final String STATUS_STR_READ = "read";
    private int status = 1;

    public ChatBased(){}

    public ChatBased(JSONObject jo) {
        this.messageTime = jo.optLong("messageTime");
        this.messageText = jo.optString("messageText");
        this.chatPhoto = jo.has("photo") ? jo.optString("photo") : jo.optString("chatPhoto");
        this.name = jo.has("name") ? jo.optString("name") : jo.optString("chatName");
        this.chatId = jo.has("chatId") ? jo.optString("chatId") : User.P2P_CHAT_PREFIX + jo.optString("userId");
        if (jo.has("isFavorite")) this.isFavorite = jo.optBoolean("isFavorite");
        if (jo.has("options")) {
            JSONObject ojo = jo.optJSONObject("options");
            isBlocked = ojo.optBoolean("block", false);
            isFavorite = ojo.optBoolean("fav", false);
            try {
                isMute = "all".equals(ojo.getJSONObject("ntf").getString("m"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        countUnread = (isBlocked || isMute) ? 0 : jo.optInt("unread", 0);
        countUnread = isBlocked ? 0 : jo.optInt("unread", 0);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getChatPhoto() {
        return chatPhoto;
    }

    public void setChatPhoto(String chatPhoto) {
        this.chatPhoto = chatPhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getCountUnread() {
        return countUnread;
    }

    public static int parseStatus(String status) {
        if (STATUS_STR_DELIV.equals(status)) return STATUS_DELIV;
        if (STATUS_STR_READ.equals(status)) return STATUS_READ;
        if (STATUS_STR_NONE.equals(status)) return STATUS_NONE;
        return STATUS_NONE;
    }

    public void setCountUnread(int countUnread) {
        this.countUnread = countUnread;
    }

    public static void sort(List<ChatBased> chats) {
        Collections.sort(chats, new Comparator<ChatBased>() {
            @Override
            public int compare(ChatBased t0, ChatBased t1) {
                return t0.getMessageTime() < t1.getMessageTime() ? 1 : -1;
            }
        });
    }
}
