package mobi.sender.model.msg;

/**
 * Created by Smmarat on 25.07.16.
 */
public class MediaMessage extends MsgBased {

    public static final String TYPE_VIDEO = "mp4";

    private String name;
    private String url;
    private String type;
    private String preview;
    private String desc;
    private String lentgh;

    public MediaMessage(String name, String url, String type, String preview, String desc, String length, String chatId) {
        super(chatId);
        this.name = name;
        this.url = url;
        this.type = type;
        this.preview = preview;
        this.desc = desc;
        this.lentgh = length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLentgh() {
        return lentgh;
    }

    public void setLentgh(String lentgh) {
        this.lentgh = lentgh;
    }
}
