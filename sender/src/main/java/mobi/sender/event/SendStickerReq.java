package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SendStickerReq implements Bus.Event {

    private String id, chatId;

    public SendStickerReq(String id, String chatId) {
        this.id = id;
        this.chatId = chatId;
    }

    public String getId() {
        return id;
    }

    public String getChatId() {
        return chatId;
    }
}
