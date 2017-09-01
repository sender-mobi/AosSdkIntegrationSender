package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SetActStateReq implements Bus.Event {

    private String chatId;
    private boolean active;

    public SetActStateReq(String chatId, boolean active) {
        this.chatId = chatId;
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public String getChatId() {
        return chatId;
    }
}
