package discord_app_client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

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

@Named("blockedUsers")
@SessionScoped
public class BlockedUsers implements Serializable {

    @Inject
    private SessionedUser sessionedUser;
    private Client client;
    private WebTarget base;
    private String message;
    private String userToBlock;
    private List<User> blockedUsers;

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

    public void loadBlockedUsers() {
        System.out.println("Loading blocked users list...");

        if (sessionedUser == null || sessionedUser.getUserUid() == null) {
            message = "Error occurred while loading list of blocked users";
            return;
        }

        try {
            WebTarget blockedUserListTarget = base.path("block/list");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_uid", sessionedUser.getUserUid());

            HashMap<String, Object> response = blockedUserListTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            blockedUsers = (List<User>) response.get("blocklist");
            System.out.println(blockedUsers);

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while loading list of blocked users";
        }
    }

    public void blockThisUser() {
        if (userToBlock == null || userToBlock.trim().isEmpty()) {
            message = "Please enter a username to block";
            return;
        }

        if (sessionedUser == null || sessionedUser.getUserUid() == null) {
            message = "You must be logged in";
            return;
        }

        try {
            WebTarget blockUserTarget = base.path("block/user");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_uid", sessionedUser.getUserUid());
            requestBody.put("user_to_block", userToBlock);

            HashMap<String, Object> response = blockUserTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            message = (String) response.get("message");
            System.out.println(message);

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while loading list of blocked users";
        }
    }

    public List<User> getBlockedUsers() {
        return blockedUsers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBlockedUsers(List<User> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public String getUserToBlock() {
        return userToBlock;
    }

    public void setUserToBlock(String userToBlock) {
        this.userToBlock = userToBlock;
    }

}
