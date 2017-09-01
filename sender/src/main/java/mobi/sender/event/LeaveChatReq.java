package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class LeaveChatReq implements Bus.Event {

    private String chatId;

    public LeaveChatReq(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }
}
