package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Zver on 17.11.2016.
 */

public class AlertReq implements Bus.Event{
    String chatId;

    public AlertReq(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }
}
