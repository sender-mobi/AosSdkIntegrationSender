package mobi.sender.event;

/**
 * Created by Zver on 03.02.2017.
 */

public class ChatInfoReq extends SyncEvent {

    private String chatId;

    public ChatInfoReq(String chatId, SRespListener listener) {
        super(listener);
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }
}
