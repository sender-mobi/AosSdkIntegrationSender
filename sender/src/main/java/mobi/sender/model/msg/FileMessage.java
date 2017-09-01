package mobi.sender.model.msg;

public class FileMessage extends MsgBased {

    private long size;
    private String name;
    private String url;
    private String type;


    public FileMessage(long size, String name, String url, String type, String chatId) {
        super(chatId);
        this.size = size;
        this.name = name;
        this.url = url;
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
