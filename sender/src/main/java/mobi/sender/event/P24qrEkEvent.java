package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Zver on 08.02.2017.
 */

public class P24qrEkEvent implements Bus.Event {

    private String str;

    public P24qrEkEvent(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }
}
