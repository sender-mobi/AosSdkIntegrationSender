package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SendOnlineReq implements Bus.Event {

    private String key;

    public SendOnlineReq(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
