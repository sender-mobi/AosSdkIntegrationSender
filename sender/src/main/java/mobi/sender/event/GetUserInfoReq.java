package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class GetUserInfoReq extends SyncEvent {

    private String userId;

    public GetUserInfoReq(String userId, SRespListener listener) {
        super(listener);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
