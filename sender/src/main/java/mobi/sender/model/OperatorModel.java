package mobi.sender.model;

/**
 * Created by Zver on 04.11.2016.
 */

public class OperatorModel {
    private String name;
    private String photo;
    private String userId;

    public OperatorModel(String name, String photo, String userId) {
        this.name = name;
        this.photo = photo;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
