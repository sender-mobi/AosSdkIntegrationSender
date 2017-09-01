package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class UpdateCtReq extends SyncEvent {

    private String userId, name;
    private boolean isAdded;

    public UpdateCtReq(String userId, String name, boolean isAdded, SRespListener listener) {
        super(listener);
        this.userId = userId;
        this.name = name;
        this.isAdded = isAdded;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public boolean isAdded() {
        return isAdded;
    }
}
