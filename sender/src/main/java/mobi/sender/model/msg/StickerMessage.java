package mobi.sender.model.msg;

public class StickerMessage extends MsgBased {

    private String image;

    public StickerMessage(String image, String chatId) {
        super(chatId);
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
