package mobi.sender.event;

import android.net.Uri;

import mobi.sender.Bus;

/**
 * Created by Zver on 29.11.2016.
 */

public class CreateGroupChatReq  extends SyncEvent {

    private Uri photoUri;
    private String name;
    private String[] arrayUserId;

    public CreateGroupChatReq(Uri photoUri, String name, String[] arrayUserId, SRespListener listener) {
        super(listener);
        this.photoUri = photoUri;
        this.name = name;
        this.arrayUserId = arrayUserId;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getArrayUserId() {
        return arrayUserId;
    }

    public void setArrayUserId(String[] arrayUserId) {
        this.arrayUserId = arrayUserId;
    }
}
