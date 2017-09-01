package mobi.sender.model.msg;

public class AudioMessage extends MsgBased {

    private String name;
    private String url;
    private String type;
    private String desc;
    private int length;

    public AudioMessage(String name, String url, String type, String desc, int length, String chatId) {
        super(chatId);
        this.name = name;
        this.url = url;
        this.type = type;
        this.desc = desc;
        this.length = length;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
