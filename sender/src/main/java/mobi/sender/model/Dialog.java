package mobi.sender.model;

import org.json.JSONObject;

/**
 * Created by vp on 14.07.16.
 */
public class Dialog extends ChatBased{

    private String encrKey;
    private boolean isOperator = false;
    private String companyId = "";
    private String senderKey;

    public Dialog() {
    }

    public Dialog(JSONObject jo) {
        super(jo);
        if(jo.has("encryptionKey")) {
            this.encrKey = jo.optString("encryptionKey", "");
        }else if (jo.has("encrKey")){
            this.encrKey = jo.optString("encrKey", "");
        }
        this.senderKey = jo.optString("senderKey", "");
        this.isOperator = "oper".equals(jo.optString("type"));
        if (isOperator) this.companyId = jo.optString("companyId");

    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public boolean isOperator() {
        return isOperator;
    }

    public void setOperator(boolean operator) {
        isOperator = operator;
    }

    public String getEncrKey() {
        return encrKey;
    }

    public void setEncrKey(String encrKey) {
        this.encrKey = encrKey;
    }

    public String getSenderKey() {
        return senderKey;
    }
}
