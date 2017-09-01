package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SendQrReq extends SyncEvent {

    private String qr;

    public SendQrReq(String qr, SRespListener listener) {
        super(listener);
        this.qr = qr;
    }

    public String getQr() {
        return qr;
    }
}
