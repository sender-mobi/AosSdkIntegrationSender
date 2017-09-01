package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class GetCtByPhoneReq extends SyncEvent {

    private String phone;

    public GetCtByPhoneReq(String phone, SRespListener listener) {
        super(listener);
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }
}
