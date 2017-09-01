package mobi.sender.event;

import mobi.sender.Bus;

public class TypingEvent implements Bus.Event {

    private String chatId, from;

    public TypingEvent(String chatId, String from) {
        this.chatId = chatId;
        this.from = from;
    }

    public String getChatId() {
        return chatId;
    }

    public String getFrom() {
        return from;
    }
}
