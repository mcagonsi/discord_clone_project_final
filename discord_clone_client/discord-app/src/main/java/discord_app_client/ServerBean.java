package discord_app_client;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_app_client.models.Server;
import discord_app_client.models.User;
import discord_app_client.utils.Variables;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

@Named("serverBean")
@RequestScoped
public class ServerBean implements Serializable {
    private Client client;
    private WebTarget base;

    @Inject
    private SessionedUser sessionedUser;

    private String serverName;
    private String serverDescription;
    private boolean serverPublicStatus;

    private String inviteLink;

    private Server privateServer;

    @Inject
    private PublicServerSearchResults publicServerSearchResults;

    private String message;

    private String searchQuery;

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


    public void clearSearchResults() {
        publicServerSearchResults.setServers(new ArrayList<>());
        message = null;
    }

    public String searchPublicServers() {

        System.out.println("Searching for public servers..." + searchQuery);
        try {
            WebTarget serverSearchTarget = base.path("servers/search");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("search", searchQuery);
            System.out.println("Request Body: " + requestBody);

            HashMap<String, Object> response = serverSearchTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            System.out.println(response.get("servers"));
            if (response.get("servers") != null) {
                publicServerSearchResults.setServers((List<Server>) response.get("servers"));
            } else {
                message = (String) response.get("message");
            }
            System.out.println(publicServerSearchResults.getServers());
        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while loading servers list";
        }

        return null;
    }

    public String joinServer(HashMap<String, Object> server) {
        System.out.println("joinServer called with server: " + server);
        if (server == null) {
            message = "Invalid server selected";
            return null;
        }
        System.out.println("Joining server: " + server.get("name"));

        try {
            WebTarget joinServerTarget = base.path("servers/join");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("serverId", server.get("id").toString());
            requestBody.put("inviteCode", server.get("inviteCode"));

            HashMap<String, Object> user = new HashMap<>();
            user.put("uid", sessionedUser.getUserUid());
            user.put("token", sessionedUser.getToken());

            requestBody.put("user", user);
            System.out.println("Request Body: " + requestBody);

            HashMap<String, Object> response = joinServerTarget
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(requestBody), HashMap.class);

            message = (String) response.get("message");

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while joining server";
        }
        return null;
    }

    public String fetchServerInfo() {
        System.out.println("fetchServerInfo called");
        return null;
    }

    public String acceptServerInviteOrJoinPrivateServer(HashMap<String, Object> server) {
        System.out.println("acceptServerInviteOrJoinPrivateServer called with server: " + server);
        return null;
    }

    public String createServer() {
        System.out.println("createServer called with server: " + serverName);

        try {
        WebTarget createsServerTarget = base.path("servers/create");

        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_uid", sessionedUser.getUserUid());
        requestBody.put("token", sessionedUser.getToken());
        requestBody.put("name", serverName);
        requestBody.put("description", serverDescription);
        requestBody.put("is_public", serverPublicStatus ? true : false);

        
        System.out.println("Request Body: " + requestBody);

        HashMap<String, Object> response = createsServerTarget
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.json(requestBody), HashMap.class);

        message = (String) response.get("message");

        } catch (Exception e) {
        e.printStackTrace();
        message = "Error occurred while creating server";
        }
        return null;
    }

    public List<Server> getServersSearchResult() {
        return publicServerSearchResults.getServers();
    }

    public void setServerSearchResult(List<Server> serversSearchResult) {
        publicServerSearchResults.setServers(serversSearchResult);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerDescription() {
        return serverDescription;
    }

    public void setServerDescription(String serverDescription) {
        this.serverDescription = serverDescription;
    }

    public boolean isServerPublicStatus() {
        return serverPublicStatus;
    }

    public void setServerPublicStatus(boolean serverPublicStatus) {
        this.serverPublicStatus = serverPublicStatus;
    }

    public String getInviteLink() {
        return inviteLink;
    }

    public void setInviteLink(String inviteLink) {
        this.inviteLink = inviteLink;
    }

    public Server getPrivateServer() {
        return privateServer;
    }
    public void setPrivateServer(Server privateServer) {
        this.privateServer = privateServer;
    }
}