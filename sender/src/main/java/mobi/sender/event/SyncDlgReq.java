package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SyncDlgReq extends SyncEvent {

    private boolean isFullVersion;

    public SyncDlgReq(boolean isFullVersion, SRespListener listener) {
        super(listener);
        this.isFullVersion = isFullVersion;
    }

    public boolean isFullVersion() {
        return isFullVersion;
    }
}
