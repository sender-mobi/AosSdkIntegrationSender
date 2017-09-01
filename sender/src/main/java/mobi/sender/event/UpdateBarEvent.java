package mobi.sender.event;

import mobi.sender.Bus;

public class UpdateBarEvent implements Bus.Event {
    private String chatId;

    public UpdateBarEvent(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }
}
