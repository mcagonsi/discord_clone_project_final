package discord_rest_api.models;

import java.io.Serializable;

public class ServerMember implements Serializable{
    private User user;
    private Role role;

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
