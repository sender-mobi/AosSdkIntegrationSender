package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class UploadFileReq extends SyncEvent {

    private String path;

    public UploadFileReq(String path, SRespListener listener) {
        super(listener);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
