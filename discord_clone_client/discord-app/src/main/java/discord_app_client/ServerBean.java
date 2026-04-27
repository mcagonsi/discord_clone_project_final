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
@SessionScoped
public class ServerBean implements Serializable {
    private Client client;
    private WebTarget base;

    @Inject
    private SessionedUser sessionedUser;

    private String serverName;
    private String serverDescription;
    private boolean serverPublicStatus;

    private String inviteLink;

    @Inject
    private PrivateServerToJoin privateServer;

    @Inject
    private PublicServerSearchResults publicServerSearchResults;

    private String message;

    private String searchQuery;

    private List<Server> joinedServersList = new ArrayList<>();

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

    private HashMap<String, String> extractIdAndinviteCodeFromInviteLink(String inviteLink) {
        HashMap<String, String> result = new HashMap<>();
        if (inviteLink != null && inviteLink.startsWith(Variables.DOMAIN_URL)) {
            String path = inviteLink.substring(Variables.DOMAIN_URL.length()); // Slice out the domain
            String[] parts = path.split("/");
            if (parts.length >= 2) {
                result.put("id", parts[0]);
                result.put("inviteCode", parts[1]);
            }
        }
        return result;
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
        if (inviteLink == null || inviteLink.trim().isEmpty()) {
            message = "Please enter an invite link";
            return null;
        }
        if (inviteLink != null && !inviteLink.trim().isEmpty()) {
            HashMap<String, String> serverInfo = extractIdAndinviteCodeFromInviteLink(inviteLink);
            System.out.println("Server Info: " + serverInfo);
            try {
                WebTarget serverInfoTarget = base.path("servers/info");

                HashMap<String, Object> requestBody = new HashMap<>();
                requestBody.put("serverId", serverInfo.get("id"));
                requestBody.put("inviteCode", serverInfo.get("inviteCode"));
                System.out.println("Request Body: " + requestBody);

                HashMap<String, Object> response = serverInfoTarget
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json(requestBody), HashMap.class);

               
                if (response.get("serverInfo") != null) {
                    HashMap<String,Object>privateServerInfo = (HashMap<String,Object>) response.get("serverInfo");
                    System.out.println("Private Server Info: " + privateServerInfo);
                    privateServer.setName((String) privateServerInfo.get("name"));
                    privateServer.setDescription((String) privateServerInfo.get("description"));
                    privateServer.setInviteCode((String) privateServerInfo.get("inviteCode"));
                    privateServer.setId(((Number) privateServerInfo.get("id")).intValue());
                    privateServer.setPublicStatus((Boolean) privateServerInfo.get("public"));
                }
                else {
                    message = (String) response.get("message");
                }

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error occurred while creating server";
            }

        }
        
        return null;
    }

    public String acceptServerInviteOrJoinPrivateServer() {
        System.out.println("acceptServerInviteOrJoinPrivateServer called");
        if (privateServer == null) {
            message = "No private server information available";
            return null;
        }

        try {
            WebTarget acceptInviteTarget = base.path("servers/join");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("serverId", (String) String.valueOf(privateServer.getId()));
            requestBody.put("inviteCode", privateServer.getInviteCode());

            HashMap<String, Object> user = new HashMap<>();
            user.put("uid", sessionedUser.getUserUid());
            user.put("token", sessionedUser.getToken());

            requestBody.put("user", user);
            System.out.println("Request Body: " + requestBody);

            HashMap<String, Object> response = acceptInviteTarget
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(requestBody), HashMap.class);

            message = (String) response.get("message");
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while accepting server invite";
        }
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

    public String fetchJoinedServers() {
        System.out.println("fetchJoinedServers called");
        if (sessionedUser == null) {
            message = "User is not logged in";
            return null;
        }
        if (sessionedUser!= null && sessionedUser.getUserUid() != null) {
            
            try {
                WebTarget joinServersTarget = base.path("servers/joined");

                HashMap<String, Object> requestBody = new HashMap<>();
                requestBody.put("user_uid", sessionedUser.getUserUid());
                requestBody.put("token", sessionedUser.getToken());
                System.out.println("Request Body: " + requestBody);

                HashMap<String, Object> response = joinServersTarget
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json(requestBody), HashMap.class);

               if (response.get("joinedServers") != null) {
                //    joinedServersList.clear();
                   for (HashMap<String, Object> serverInfo : (List<HashMap<String, Object>>) response.get("joinedServers")) {
                       System.out.println("Joined Server Info: " + serverInfo);
                       Server server = new Server();
                       server.setName((String) serverInfo.get("name"));
                       server.setDescription((String) serverInfo.get("description"));
                       server.setInviteCode((String) serverInfo.get("inviteCode"));
                       server.setId(((Number) serverInfo.get("id")).intValue());
                       server.setPublicStatus((Boolean) serverInfo.get("public"));
                       joinedServersList.add(server);
                   }
                }
                else {
                    message = (String) response.get("message");
                }

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error occurred while creating server";
            }

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
    public void setPrivateServer(PrivateServerToJoin privateServer) {
        this.privateServer = privateServer;
    }
    public List<Server> getJoinedServersList() {
        return joinedServersList;
    }
    public void setJoinedServersList(List<Server> joinedServersList) {
        this.joinedServersList = joinedServersList;
    }
}