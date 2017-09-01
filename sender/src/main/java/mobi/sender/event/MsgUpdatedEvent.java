package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class MsgUpdatedEvent implements Bus.Event {

    private String chatId;

    public MsgUpdatedEvent(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

}
