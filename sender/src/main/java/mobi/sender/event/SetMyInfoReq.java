package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SetMyInfoReq extends SyncEvent {

    private String name, desc, photo;

    public SetMyInfoReq(String name, String desc, String photo, SRespListener listener) {
        super(listener);
        this.name = name;
        this.desc = desc;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getPhoto() {
        return photo;
    }
}
