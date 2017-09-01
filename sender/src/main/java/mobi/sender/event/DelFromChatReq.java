package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class DelFromChatReq extends SyncEvent {

    private String chatId, userId;

    public DelFromChatReq(String chatId, String userId, SRespListener listener) {
        super(listener);
        this.chatId = chatId;
        this.userId = userId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getUserId() {
        return userId;
    }
}
