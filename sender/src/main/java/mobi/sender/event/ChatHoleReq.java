package mobi.sender.event;

/**
 * Created by Zver on 30.01.2017.
 */

public class ChatHoleReq extends SyncEvent {

    private String chatId;
    private long topPacketId;
    private long bothPacketId;

    public ChatHoleReq(String chatId, long topPacketId, long bothPacketId, SRespListener listener) {
        super(listener);
        this.chatId = chatId;
        this.topPacketId = topPacketId;
        this.bothPacketId = bothPacketId;
    }

    public String getChatId() {
        return chatId;
    }

    public long getTop() {
        return topPacketId;
    }

    public long getBottom() {
        return bothPacketId;
    }
}
