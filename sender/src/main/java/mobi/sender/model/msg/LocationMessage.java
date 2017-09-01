package mobi.sender.model.msg;

public class LocationMessage extends MsgBased {

    private String preview;
    private String textMsg;
    private double lat;
    private double lon;

    public LocationMessage(String preview, String textMsg, double lat, double lon, String chatId) {
        super(chatId);
        this.preview = preview;
        this.textMsg = textMsg;
        this.lat = lat;
        this.lon = lon;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getTextMsg() {
        return textMsg;
    }

    public void setTextMsg(String textMsg) {
        this.textMsg = textMsg;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
