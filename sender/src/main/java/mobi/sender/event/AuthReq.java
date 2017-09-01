package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class AuthReq extends SyncEvent {

    private String action, param;

    public AuthReq(String action, String param, SRespListener listener) {
        super(listener);
        this.action = action;
        this.param = param;
    }

    public String getAction() {
        return action;
    }

    public String getParam() {
        return param;
    }
}
