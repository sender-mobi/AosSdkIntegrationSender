package mobi.sender.event;

/**
 * Created by Smmarat on 07.09.16.
 */
public class GetCompanyOperatorsReq extends SyncEvent {

    private String compId;

    public GetCompanyOperatorsReq(String compId, SRespListener listener) {
        super(listener);
        this.compId = compId;
    }

    public String getCompId() {
        return compId;
    }
}
