package discord_rest_api.models;

import java.io.Serializable;

public class Permission implements Serializable {
    
    private int id;
    private  String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}