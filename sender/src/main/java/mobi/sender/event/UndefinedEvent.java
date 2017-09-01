package mobi.sender.event;

import org.json.JSONObject;

import mobi.sender.Bus;

public class UndefinedEvent implements Bus.Event {

    private JSONObject data;

    public UndefinedEvent(JSONObject data) {
        this.data = data;
    }

    public JSONObject getData() {
        return data;
    }
}
