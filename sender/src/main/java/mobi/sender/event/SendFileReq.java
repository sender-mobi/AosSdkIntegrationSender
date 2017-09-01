package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SendFileReq implements Bus.Event {

    private String chatId, name, path, type, description;

    public SendFileReq(String chatId, String name, String description, String path, String type) {
        this.chatId = chatId;
        this.name = name;
        this.path = path;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getChatId() {
        return chatId;
    }
}
