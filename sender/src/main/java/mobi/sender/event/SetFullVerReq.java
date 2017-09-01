package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SetFullVerReq extends SyncEvent {

    private boolean fullVer;

    public SetFullVerReq(boolean fullVer, SRespListener listener) {
        super(listener);
        this.fullVer = fullVer;
    }

    public boolean isFullVer() {
        return fullVer;
    }
}
