package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class EditMsgReq extends SyncEvent {

    private String chatId, text;
    private long packetId;

    public EditMsgReq(String chatId, String text, long packetId, SRespListener listener) {
        super(listener);
        this.chatId = chatId;
        this.text = text;
        this.packetId = packetId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getText() {
        return text;
    }

    public long getPacketId() {
        return packetId;
    }
}
