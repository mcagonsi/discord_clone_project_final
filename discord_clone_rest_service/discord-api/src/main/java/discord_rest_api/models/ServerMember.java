package discord_rest_api.models;

import java.io.Serializable;
import java.util.List;

public class ServerMember implements Serializable{
    private User user;
    private List<Role> role;

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public List<Role> getRole() {
        return role;
    }

    public void setRole(List<Role> role) {
        this.role = role;
    }
}
