package mobi.sender.event;

import org.json.JSONObject;

import mobi.sender.Bus;

/**
 * Created by Smmarat on 07.09.16.
 */
public class AuthEvent implements Bus.Event {

    private JSONObject model;

    public AuthEvent(JSONObject model) {
        this.model = model;
    }

    public JSONObject getModel() {
        return model;
    }
}
