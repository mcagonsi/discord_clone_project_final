package discord_app_client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;


import discord_app_client.models.User;
import discord_app_client.utils.Variables;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

@Named("userFriendsBean")
@RequestScoped
public class FriendsBean implements Serializable{
    @Inject
    private SessionedUser sessionedUser;
    private String username;
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
            message = "User not logged in";
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
            if (response.get("friendslist") != null) {
                friends = (List<User>) response.get("friendslist");
            }
            else {
                message = (String) response.get("message");
            }
            System.out.println(friends);

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while loading friends list";
        }
    }

    public void sendFriendRequest() {
        System.out.println("Sending friend request...");
        if (sessionedUser == null || sessionedUser.getUserUid() == null) {
            message = "User not logged in";
            return;
        }
        if (username == null || username.trim().isEmpty()) {
            message = "Invalid friend username";
            return;
        }

        try {
            WebTarget sendRequestTarget= base.path("friends/sendRequest");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_uid", sessionedUser.getUserUid());
            requestBody.put("friend", username);

            HashMap<String, Object> response = sendRequestTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            message = (String) response.get("message");

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while sending friend request";
        }
    }

    public List<User> getFriends() {
        return friends;
    }
    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

}
