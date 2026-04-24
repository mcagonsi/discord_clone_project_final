package discord_app_client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.print.attribute.standard.Media;

import discord_app_client.models.User;
import discord_app_client.utils.Variables;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

@Named("userFriends")
@SessionScoped
public class UserFriends implements Serializable{
    @Inject
    private SessionedUser sessionedUser;
    private String friend_username;
    private Client client;
    private WebTarget base;

    private String message;
    
    private List<User> friends;


    @PostConstruct
    public void init() {
        client = ClientBuilder.newClient();
        base = client.target(Variables.API_URL);
    }

    @PreDestroy
    public void cleanup() {
        if (client != null) {
            client.close();
        }
    }

    public void loadFriendsList() {
        System.out.println("Loading friends list...");

        if (sessionedUser == null || sessionedUser.getUserUid() == null) {
            message = "Error occurred while loading friends list";
            return;
        }

        try {
            WebTarget friendsListTarget = base.path("friends/list");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_uid", sessionedUser.getUserUid());

            HashMap<String, Object> response = friendsListTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            System.out.println(response.get("friendslist"));

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while loading friends list";
        }
    }

    public List<User> getUserFriends() {
        return friends;
    }
    public void setUserFriends(List<User> friends) {
        this.friends = friends;
    }

    public String getFriend_username() {
        return friend_username;
    }

    public void setFriend_username(String friend_username) {
        this.friend_username = friend_username;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

}
