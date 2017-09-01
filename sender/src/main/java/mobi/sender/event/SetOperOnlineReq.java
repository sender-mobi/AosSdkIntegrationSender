package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SetOperOnlineReq implements Bus.Event {

    private boolean online;

    public SetOperOnlineReq(boolean online) {
        this.online = online;
    }

    public boolean isOnline() {
        return online;
    }
}
