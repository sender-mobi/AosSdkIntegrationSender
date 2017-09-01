package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SetDialogCryptStateReq implements Bus.Event {

    private boolean encrypted;
    private String chatId;

    public SetDialogCryptStateReq(String chatId, boolean encrypted) {
        this.encrypted = encrypted;
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public boolean isEncrypted() {
        return encrypted;
    }
}
