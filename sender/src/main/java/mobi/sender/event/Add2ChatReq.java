package mobi.sender.event;

import org.json.JSONArray;

/**
 * Created by Smmarat on 07.09.16.
 */
public class Add2ChatReq extends SyncEvent {

    private JSONArray arr;
    private String chatId;

    public Add2ChatReq(String chatId, JSONArray arr, SRespListener listener) {
        super(listener);
        this.arr = arr;
        this.chatId = chatId;
    }

    public JSONArray getArr() {
        return arr;
    }

    public String getChatId() {
        return chatId;
    }
}
