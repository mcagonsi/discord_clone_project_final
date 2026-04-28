package discord_app_client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_app_client.models.Channel;
import discord_app_client.models.DirectChatMessage;
import discord_app_client.models.Server;
import discord_app_client.models.ServerChat;
import discord_app_client.models.ServerChatMessage;
import discord_app_client.models.ServerMember;
import discord_app_client.models.User;
import discord_app_client.utils.Variables;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

@Named("serverChatBean")
@SessionScoped
public class ServerChatBean implements Serializable {
    
    
    @Inject
    private SessionedUser sessionedUser;

    @Inject
    private DashboardNavigationState dashboardNavigationState;

    //TODO: make this work
    private List<ServerMember> serverMembers = new ArrayList<>();

    private Client client;
    private WebTarget base;

    private String message;

    private ServerChat serverChat = new ServerChat();
    private Part attachmentFile;
    private String messageContent;
    private List<ServerChatMessage> serverChannelMessages = new ArrayList<>();



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


    public String loadChannelList() {

        try {
            WebTarget userDirectChatTarget = base.path("channel/list");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_uid", sessionedUser.getUserUid());
            requestBody.put("server_id", String.valueOf(dashboardNavigationState.getServerId()));

            HashMap<String, Object> response = userDirectChatTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("channels") != null) {
                serverChat.clear();
                System.out.println(response.get("channels"));
                serverChat.setChannels((List<Channel>) response.get("channels"));

            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while loading direct chat list";
        }

        return null;
    }

    public void loadChannelMessages() {
        System.out.println("loading channel messages");
        System.out.println("the server channel page is getting loaded");
        // try {
        //     WebTarget channelMessagesTarget = base.path("channel/messages");

        //     HashMap<String, Object> requestBody = new HashMap<>();
        //     requestBody.put("channel_id", String.valueOf(dashboardNavigationState.getChannelId()));
        //     requestBody.put("user_uid", sessionedUser.getUserUid());

        //     HashMap<String, Object> response = channelMessagesTarget
        //             .request(MediaType.APPLICATION_JSON)
        //             .post(Entity.json(requestBody), HashMap.class);

        //     if (response.get("messages") != null) {
        //         serverChannelMessages.clear();
        //         System.out.println(response.get("messages"));
        //         serverChannelMessages.addAll((List<ServerChatMessage>) response.get("messages"));
        //     }
        //     return null;
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     message = "Error occurred while loading channel messages";
        // }
       
    }

        public String loadServerMembers() {
        try {
                WebTarget serverMembersListTarget = base.path("servermembers/list");

                HashMap<String, Object> requestBody = new HashMap<>();
                requestBody.put("server_id", "" + dashboardNavigationState.getServerId());
                System.out.println("Request Body: " + requestBody);

                HashMap<String, Object> response = serverMembersListTarget
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json(requestBody), HashMap.class);

               if (response.get("servermembers") != null) {
                   serverMembers.clear();
                   System.out.println(response.get("servermembers"));
                   //serverChat.setChannels((List<Channel>) response.get("channels"));
                   serverMembers = (List<ServerMember>) response.get("servermembers");
                   System.out.println(serverMembers);
                }
                else {
                    message = (String) response.get("message");
                }

            } catch (Exception e) {
                e.printStackTrace();
                message = "Error occurred while creating server";
            }
            return null;
    }

    public ServerChat getServerChat() {
        return serverChat;
    }

    public void setServerChat(ServerChat serverChat) {
        this.serverChat = serverChat;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
    
    public Part getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(Part attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    public List<ServerChatMessage> getServerChannelMessages() {
        return serverChannelMessages;
    }

    public void setServerChannelMessages(List<ServerChatMessage> serverChannelMessages) {
        this.serverChannelMessages = serverChannelMessages;
    }
    
    public SessionedUser getSessionedUser() {
        return sessionedUser;
    }
    public void setSessionedUser(SessionedUser sessionedUser) {
        this.sessionedUser = sessionedUser;
    }

    public List<ServerMember> getServerMembers() {
        return serverMembers;
    }
    public void setServerMembers(List<ServerMember> serverMembers) {
        this.serverMembers = serverMembers;
    }
}

