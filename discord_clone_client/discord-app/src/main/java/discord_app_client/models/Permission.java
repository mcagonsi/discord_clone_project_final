package discord_app_client.models;

import java.io.Serializable;

public class Permission  implements Serializable{
    private int id;
    private String name;


    public static Permission makePermission(int id, String name) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setName(name);
        return permission;
    }

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
