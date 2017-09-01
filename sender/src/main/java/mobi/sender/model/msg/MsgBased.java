package mobi.sender.model.msg;

import android.content.Context;

import com.sender.library.ChatFacade;

import org.json.JSONException;
import org.json.JSONObject;

import mobi.sender.R;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;

/**
 * Created by Smmarat on 25.07.16.
 */
public class MsgBased {

    public static final int STATUS_NEW = 0;
    public static final int STATUS_SENT = 1;
    protected String from;
    protected long created;
    protected long packetId;
    protected String chatId;
    protected String className;
    protected String model;
    protected int status;
    protected int localId;
    protected long timeVersion;

    protected boolean toOper;

    public MsgBased() {
    }

    public MsgBased(String chatId) {
        this.chatId = chatId;
    }

    public static boolean isMine(MsgBased msg, Context ctx) {
        return Storage.getInstance(ctx).getMyUserId().equals(msg.from);
    }

    public static MsgBased create(String chatId, long packetId, int status, String className, String view,
                                  String procId, String model, String from, long created, int toOper, int localId,
                                  long timeVersion, byte [] byteArr) {
        MsgBased m = null;
        try {
            JSONObject jom = new JSONObject(model);
            switch (className) {
                case ChatFacade.CLASS_TEXT_ROUTE:
                    boolean encr = (1 == jom.optInt("encrypted") || jom.optBoolean("encrypted"));
                    m = new TextMessage(jom.optString("text"), chatId, encr, jom.optString("pkey"), byteArr);
                    break;
                case ChatFacade.CLASS_STICKER:
                    m = new StickerMessage(jom.getString("id"), chatId);
                    break;
                case ChatFacade.CLASS_IMAGE_ROUTE:
                case ChatFacade.CLASS_VIDEO_ROUTE:
                    m = new MediaMessage(
                            jom.optString("name"),
                            jom.optString("url"),
                            jom.optString("type"),
                            jom.optString("preview"),
                            jom.optString("desc"),
                            jom.optString("length"),
                            chatId);
                    break;
                case ChatFacade.CLASS_AUDIO_ROUTE:
                    m = new AudioMessage(
                            jom.optString("name"),
                            jom.optString("url"),
                            jom.optString("type"),
                            jom.optString("desc"),
                            jom.optInt("length"),
                            chatId);
                    break;
                case ChatFacade.CLASS_SHARE_LOCATION:
                    m = new LocationMessage(
                            jom.optString("preview"),
                            jom.optString("textMsg"),
                            jom.optDouble("lat"),
                            jom.optDouble("lon"),
                            chatId);
                    break;
                case ChatFacade.CLASS_FILE_ROUTE:
                    m = new FileMessage(
                            jom.optLong("size"),
                            jom.optString("name"),
                            jom.optString("url"),
                            jom.optString("type"),
                            chatId);
                    break;
                default:
                    if (view != null && view.startsWith("{")) {
                        m = new FormMessage(view, className, chatId, procId);
                    }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (m != null) {
            m.className = className;
            m.status = status;
            m.packetId = packetId;
            m.model = model;
            m.from = from;
            m.created = created;
            m.toOper = toOper == 1;
            m.localId = localId;
            m.timeVersion = timeVersion;
        }
        return m;
    }

    public static String getTextFromModel(Context ctx, JSONObject j) {
        return getTextFromModel(ctx, j.optString("class"), j.optJSONObject("model").toString(), j.has("view"));
    }


    public static String getTextFromModel(Context ctx, String className, String ms, boolean isForm) {
        try {
            JSONObject model = new JSONObject(ms);
            switch (className) {
                case ChatFacade.CLASS_TEXT_ROUTE:
                    return (model.optBoolean("encrypted") || 1 == model.optInt("encrypted")) ? ctx.getString(R.string.stub_encr_msg) : model.optString("text");
                case ChatFacade.CLASS_IMAGE_ROUTE:
                    return ctx.getString(R.string.stub_img);
                case ChatFacade.CLASS_FILE_ROUTE:
                    return ctx.getString(R.string.stub_file);
                case ChatFacade.CLASS_STICKER:
                    return ctx.getString(R.string.stub_sticker);
                case ChatFacade.CLASS_VIDEO_ROUTE:
                    return ctx.getString(R.string.stub_video);
                case ChatFacade.CLASS_AUDIO_ROUTE:
                    return ctx.getString(R.string.stub_audio);
                case ChatFacade.CLASS_SHARE_LOCATION:
                    return ctx.getString(R.string.stub_location);
            }

            if (model.has("title") && model.optString("title").trim().length() > 0) return model.optString("title");
            if (isForm) return ctx.getString(R.string.stub_form);
            return "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ctx.getString(R.string.msg_new_message);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getPacketId() {
        return packetId;
    }

    public void setPacketId(long packetId) {
        this.packetId = packetId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getClassName() {
        return className;
    }

    public String getModel() {
        return model;
    }

    public boolean isToOper() {
        return toOper;
    }

    public void setToOper(boolean toOper) {
        this.toOper = toOper;
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    public long getTimeVersion() {
        return timeVersion;
    }

    public void setTimeVersion(long timeVersion) {
        this.timeVersion = timeVersion;
    }
}
