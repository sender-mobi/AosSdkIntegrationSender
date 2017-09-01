package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Zver on 09.11.2016.
 */

public class UpdateContacеReq implements Bus.Event {

    private String userId;
    private String name;
    String chatId;

    public UpdateContacеReq(String userId, String name, String chatId) {
        this.userId = userId;
        this.name = name;
        this.chatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getChatId() {
        return chatId;
    }
}
