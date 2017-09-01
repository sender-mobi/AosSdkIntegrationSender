package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SetStorageReq extends SyncEvent {

    private String stor;

    public SetStorageReq(String stor, SRespListener listener) {
        super(listener);
        this.stor = stor;
    }

    public String getStor() {
        return stor;
    }
}
