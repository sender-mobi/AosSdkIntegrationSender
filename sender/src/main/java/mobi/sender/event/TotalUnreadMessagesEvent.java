package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Zver on 03.02.2017.
 */

public class TotalUnreadMessagesEvent implements Bus.Event {

    private int totalMess;

    public TotalUnreadMessagesEvent(int totalMess) {
        this.totalMess = totalMess;
    }

    public int getTotalMess() {
        return totalMess;
    }
}
