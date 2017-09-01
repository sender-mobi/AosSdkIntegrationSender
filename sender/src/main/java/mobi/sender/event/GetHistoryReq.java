package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class GetHistoryReq extends SyncEvent {

    private String chatId;
    private String top;
    private String both;

    public GetHistoryReq(String chatId, String top, String both, SRespListener listener) {
        super(listener);
        this.chatId = chatId;
        this.top = top;
        this.both = both;
    }

    public String getChatId() {
        return chatId;
    }

    public String getTop() {
        return top;
    }

    public String getBoth() {
        return both;
    }
}
