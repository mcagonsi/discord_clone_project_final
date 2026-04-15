package discord_rest_api.models;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String uid;
    private String display_name;
    private String username;
    private String email;
    private String password;
    private String token;
    private String status;
    private String created_at;

    public void generateUID() {
        if (username != null && !username.isEmpty()) {
            String randomCode = java.util.UUID.randomUUID().toString().substring(0, 5);
            this.uid = username + "-" + randomCode;
        }
    }

    public int getId() {
        return id;
    }
    public String getUid() {
        return uid;
    }
    public String getDisplay_name() {
        return display_name;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getToken() {
        return token;
    }
    public String getStatus() {
        return status;
    }
    public String getCreated_at() {
        return created_at;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
