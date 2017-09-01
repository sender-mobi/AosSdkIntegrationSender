package mobi.sender.tool;

import mobi.sender.event.SyncEvent;

/**
 * Created by Smmarat on 16.09.16.
 */
public class HisReq {
    private String chatId, top, both;
    private SyncEvent.SRespListener listener;
    private int size;

    public HisReq(String chatId, String top, String both, SyncEvent.SRespListener listener) {
        this.chatId = chatId;
        this.top = top;
        this.both = both;
        this.listener = listener;
    }

    public String getChatId() {
        return chatId;
    }

    public boolean isInclude(HisReq hr) {
        if (!chatId.equals(hr.chatId)) return false;
        boolean bothInside = (both == null && hr.both == null) || both != null && hr.both != null && Long.parseLong(both) > Long.parseLong(hr.both);
        boolean topInside = (top == null && hr.top == null) || top != null && hr.top != null && Long.parseLong(top) < Long.parseLong(hr.top);
        return bothInside && topInside;
    }

    public String getTop() {
        return top;
    }

    public String getBoth() {
        return both;
    }

    public int getSize() {
        return size;
    }

    public SyncEvent.SRespListener getListener() {
        return listener;
    }

    @Override
    public String toString() {
        return "HisReq{" +
                "chatId='" + chatId + '\'' +
                ", top='" + top + '\'' +
                ", both='" + both + '\'' +
                ", listener=" + listener +
                '}';
    }
}
