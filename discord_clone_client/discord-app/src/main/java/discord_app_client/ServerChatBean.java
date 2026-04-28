package discord_app_client;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_app_client.models.Channel;
import discord_app_client.models.DirectChatMessage;
import discord_app_client.models.ServerChat;
import discord_app_client.models.ServerChatMessage;
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

    public User getUserFromId(int id) {
        try {
            WebTarget userInfoTarget = base.path("auth/userinfo");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", String.valueOf(id));

            HashMap<String, Object> response = userInfoTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("user") != null) {
                HashMap<String, Object> userMap = (HashMap<String, Object>) response.get("user");
                User user = new User();
                user.setId(((Number) userMap.get("id")).intValue());
                user.setUsername((String) userMap.get("username"));
                user.setEmail((String) userMap.get("email"));
                System.out.println("getting user from id: " + user);
                return user;
            } else {
                message = (String) response.get("message");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while fetching user info";
            return null;
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
        try {
            WebTarget channelMessagesTarget = base.path("serverchats/chatlog");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("channel_id", String.valueOf(dashboardNavigationState.getChannelId()));
            requestBody.put("user_uid", sessionedUser.getUserUid());

            HashMap<String, Object> response = channelMessagesTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("chatlog") != null) {
                serverChannelMessages.clear();
                System.out.println(response.get("chatlog"));
                serverChannelMessages.addAll((List<ServerChatMessage>) response.get("chatlog"));
                System.out.println("this is the cast server messages: " + serverChannelMessages);
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while loading channel messages";
        }

    }
    
    public String sendServerChatMessage() {
        if (messageContent == null && attachmentFile == null) {
            message = "Message content or attachment is required";
            return null;
        }

        try {
            WebTarget sendMessageTarget = base.path("serverchats/send");

            HashMap<String, Object> requestBody = new HashMap<>();
            if (attachmentFile != null) {
                InputStream fileInputStream = attachmentFile.getInputStream();
                String filepath = sessionedUser.getUserUid() + "/" + dashboardNavigationState.getChannelId()
                        + "/server/media/" + attachmentFile.getSubmittedFileName();
                Files.createDirectories(Paths.get(filepath).getParent());
                Files.copy(fileInputStream, Paths.get(filepath), StandardCopyOption.REPLACE_EXISTING);
                requestBody.put("attachmentPath", filepath);
                requestBody.put("attachmentFilename", attachmentFile.getSubmittedFileName());
                System.out.println("Attachment uploaded to: " + filepath);
                System.out.println("Attachment filename: " + attachmentFile.getSubmittedFileName());
            }
            requestBody.put("content", messageContent);
            requestBody.put("channelId", String.valueOf(dashboardNavigationState.getChannelId()));
            requestBody.put("user_uid", sessionedUser.getUserUid());

            HashMap<String, Object> response = sendMessageTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("success") != null) {
                messageContent = ""; // Clear input after successful send
                loadChannelMessages();
                System.out.println(serverChannelMessages);// Refresh chat log
            } else {
                message = (String) response.get("message");
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while sending message";
        }
        return null;
    }
    
    public void deleteMessage(int messageId) {
        System.out.println("Deleting message with ID: " + messageId);
        try {
            WebTarget deleteMessageTarget = base.path("serverchats/delete");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("message_id", String.valueOf(messageId));

            HashMap<String, Object> response = deleteMessageTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("message") != null) {
                messageContent = ""; // Clear input after successful send
                loadChannelMessages();; // Refresh chat log
            } else {
                message = (String) response.get("message");
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while sending message";
        }

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
}

