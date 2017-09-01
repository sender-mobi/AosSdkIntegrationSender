package mobi.sender.model.msg;

/**
 * Created by Smmarat on 25.07.16.
 */
public class FormMessage extends MsgBased{

    private String view;
    private String procId;

    public FormMessage(String view, String className, String chatId, String procId) {
        super(chatId);
        this.view = view;
        this.className = className;
        this.procId = procId;
    }

    public FormMessage(String chatId) {
        super(chatId);

    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getProcId() {
        return procId;
    }

    public void setProcId(String procId) {
        this.procId = procId;
    }
}
