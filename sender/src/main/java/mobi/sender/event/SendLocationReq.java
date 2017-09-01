package mobi.sender.event;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SendLocationReq implements Bus.Event {

    private String description, lat, lon, chatId;

    public SendLocationReq(String description, String lat, String lon, String chatId) {
        this.description = description;
        this.lat = lat;
        this.lon = lon;
        this.chatId = chatId;
    }

    public String getDescription() {
        return description;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getChatId() {
        return chatId;
    }
}
