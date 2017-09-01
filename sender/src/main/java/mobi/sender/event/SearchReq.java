package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SearchReq extends SyncEvent {

    private String text;

    public SearchReq(String text, SRespListener listener) {
        super(listener);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
