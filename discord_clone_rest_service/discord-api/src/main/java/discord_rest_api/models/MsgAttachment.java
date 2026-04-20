package discord_rest_api.models;

import java.io.Serializable;

public class MsgAttachment implements Serializable {

    private int id;
    private String path;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}