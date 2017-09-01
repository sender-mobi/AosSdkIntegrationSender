package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SetlocaleReq implements Bus.Event {

    private String locale;

    public SetlocaleReq(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }
}
