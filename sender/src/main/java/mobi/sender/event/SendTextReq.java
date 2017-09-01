package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SendTextReq extends SyncEvent {

    private String text;
    private boolean isToOper;
    private boolean isEncr;
    private String pKey;
    private long packetId;
    private String chatId;

    public SendTextReq(String text, boolean isToOper, boolean isEncr, String pKey, long packetId,
                       String chatId, SRespListener listener) {
        super(listener);
        this.text = text;
        this.isToOper = isToOper;
        this.isEncr = isEncr;
        this.pKey = pKey;
        this.packetId = packetId;
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public boolean isToOper() {
        return isToOper;
    }

    public boolean isEncr() {
        return isEncr;
    }

    public String getpKey() {
        return pKey;
    }

    public long getPacketId() {
        return packetId;
    }

    public String getChatId() {
        return chatId;
    }

    //    private String chatId, text;
//    private boolean toOper = false;
//
//    public SendTextReq(String chatId, String text, boolean toOper, SRespListener listener) {
//        super(listener);
//        this.chatId = chatId;
//        this.text = text;
//        this.toOper = toOper;
//    }
//
//    public String getText() {
//        return text;
//    }
//
//    public String getChatId() {
//        return chatId;
//    }
//
//    public boolean isToOper() {
//        return toOper;
//    }
}
