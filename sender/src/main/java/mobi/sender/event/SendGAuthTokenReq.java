package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SendGAuthTokenReq implements Bus.Event {

    private String token;

    public SendGAuthTokenReq(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
