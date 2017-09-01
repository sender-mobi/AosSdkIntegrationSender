package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class ChatUpdatedEvent implements Bus.Event {

    private String chatId;

    public ChatUpdatedEvent(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }
}
