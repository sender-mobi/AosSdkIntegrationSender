package mobi.sender.event;

import org.json.JSONObject;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SetChatProfileReq implements Bus.Event {

    private String name;
    private String photo;
    private String chatId;
    private String chatDesc;

    public SetChatProfileReq(String name, String photo, String chatId, String chatDesc) {
        this.name = name;
        this.photo = photo;
        this.chatId = chatId;
        this.chatDesc = chatDesc;
    }

    public String getName() {
        return name;
    }

    public String getPhoto() {
        return photo;
    }

    public String getChatId() {
        return chatId;
    }

    public String getChatDesc() {
        return chatDesc;
    }
}
