package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Zver on 13.10.2016.
 */

public class SendReadReq implements Bus.Event {

    private String chatId;
    private long packetId;

    public SendReadReq(String chatId, long packetId) {
        this.chatId = chatId;
        this.packetId = packetId;
    }

    public String getChatId() {
        return chatId;
    }

    public long getPacketId() {
        return packetId;
    }
}
