package mobi.sender.event;

import org.json.JSONObject;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class SendFormReq implements Bus.Event {

    private String className, chatId, procId;
    private JSONObject model;

    public SendFormReq(String className, String chatId, String procId, JSONObject model) {
        this.className = className;
        this.chatId = chatId;
        this.procId = procId;
        this.model = model;
    }

    public String getClassName() {
        return className;
    }

    public String getChatId() {
        return chatId;
    }

    public String getProcId() {
        return procId;
    }

    public JSONObject getModel() {
        return model;
    }
}
