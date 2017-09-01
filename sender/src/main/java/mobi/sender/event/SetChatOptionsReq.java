package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SetChatOptionsReq implements Bus.Event {

    private String chatId;
    private boolean block, favorite, mute;

    public SetChatOptionsReq(boolean block, boolean favorite, boolean mute, String chatId) {
        this.chatId = chatId;
        this.block = block;
        this.favorite = favorite;
        this.mute = mute;
    }

    public SetChatOptionsReq(boolean favorite, String chatId) {
        this.favorite = favorite;
        this.chatId = chatId;
    }

    public boolean isBlock() {
        return block;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public boolean isMute() {
        return mute;
    }

    public String getChatId() {
        return chatId;
    }
}
