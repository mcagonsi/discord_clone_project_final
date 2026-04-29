package discord_app_client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import discord_app_client.models.DirectChatMessage;
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
import jakarta.servlet.http.Part;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Named("directChatBean")
@SessionScoped
public class DirectChatBean implements Serializable {

    @Inject
    private SessionedUser sessionedUser;

    @Inject
    private DashboardNavigationState dashboardNavigationState;

    private HashMap<Integer, User> userDirectChats = new HashMap<>();

    private Client client;
    private WebTarget base;

    private String message;

    private List<DirectChatMessage> directChatMessages = new ArrayList<>();
    private User chattingWith;

    private String messageContent;
    private Part attachmentFile;

    private String userToDM;

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

    private User getUserFromId(int id) {
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

    private void sendDM(String username) {
        // Implementation for sending DM
        System.out.println("Sending DM to user: " + username);
        if (username == null || username.trim().isEmpty()) {
            message = "Invalid user selected";
            return;
        }
        try {
            // Fetch the direct chat with this user
            WebTarget directChatTarget = base.path("directchats/openorcreatedirectchat");
            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("other_user_username", username);
            requestBody.put("user_uid", sessionedUser.getUserUid());

            HashMap<String, Object> response = directChatTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("directChatId") != null) {
                int directChatId = ((Number) response.get("directChatId")).intValue();
                dashboardNavigationState.setDirectChatId(directChatId);
                dashboardNavigationState.setMainContentPanel("chatPanel");
                dashboardNavigationState.setSideBarPanel("DMs");
                loadDirectChatMessages();
                message = "";
                return;
            } else {
                message = (String) response.get("message");
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while opening chat";
        }
    }
    

    public void dmUserbyUsername() {
        if (userToDM == null || userToDM.trim().isEmpty()) {
            message = "Please enter a username to send a DM";
            return;
        }
        // Implementation for sending DM
        sendDM(userToDM);
    }

    public void dmFromFriendList(String username) {
        System.out.println("Sending DM to user from list: " + username);
        if (username == null || username.trim().isEmpty()) {
            message = "Invalid user selected";
            return;
        }

        sendDM(username);
    }
    
    public void testDirectMessageFromFriends() {
        System.out.println("Testing direct message from friends...");
    }

    public String loadUsersDirectChats() {
        
        if (sessionedUser == null || sessionedUser.getUserUid() == null) {
            message = "User not logged in";
            return null;
        }
        try {
            WebTarget userDirectChatTarget = base.path("directchats/list");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_uid", sessionedUser.getUserUid());

            HashMap<String, Object> response = userDirectChatTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("directChatList") != null) {
                userDirectChats.clear(); // Clear previous chats
                System.out.println(response.get("directChatList"));
                for (HashMap<String, Object> chat : (List<HashMap<String, Object>>) response.get("directChatList")) {
                    int chatId = ((Number) chat.get("id")).intValue();
                    int senderId = ((Number) chat.get("senderId")).intValue();
                    int receiverId = ((Number) chat.get("receiverId")).intValue();

                    User chatUser = null;
                    if (senderId == sessionedUser.getId()) {
                        chatUser = getUserFromId(receiverId);
                    } else {
                        chatUser = getUserFromId(senderId);
                    }

                    if (chatUser != null) {
                        userDirectChats.put(chatId, chatUser);
                    }
                }
            } else {
                message = (String) response.get("message");
            }
            System.out.println(userDirectChats);

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while loading direct chat list";
        }

        return null;
    }

    public void loadDirectChatMessages(){
        if (dashboardNavigationState.getDirectChatId() == 0) {
            message = "No direct chat selected";
            return;
        }
        if (sessionedUser == null || sessionedUser.getUserUid() == null) {
            message = "User not logged in";
            return;
        }
        directChatMessages.clear(); 
        try {
            WebTarget directChatMessagesTarget = base.path("directchats/chatlog");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("directChatId", String.valueOf(dashboardNavigationState.getDirectChatId()));
            requestBody.put("user_uid", sessionedUser.getUserUid());

            HashMap<String, Object> response = directChatMessagesTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("chatlog") != null) {
                // Process messages
                for (HashMap<String, Object> msg : (List<HashMap<String, Object>>) response.get("chatlog")) {
                    DirectChatMessage chatMessage = new DirectChatMessage();
                    chatMessage.setId(((Number) msg.get("id")).intValue());
                    chatMessage.setContent((String) msg.get("content"));
                    chatMessage.setAuthorId(((Number) msg.get("authorId")).intValue());
                    chatMessage.setCreatedAt((String) msg.get("createdAt"));
                    chatMessage.setDirectChatId(dashboardNavigationState.getDirectChatId());
                    directChatMessages.add(chatMessage);
                }
                System.out.println(directChatMessages);
                if (response.get("other_user") != null) {
                    HashMap<String, Object> userMap = (HashMap<String, Object>) response.get("other_user");
                    chattingWith = new User();
                    chattingWith.setId(((Number) userMap.get("id")).intValue());
                    chattingWith.setUsername((String) userMap.get("username"));
                    chattingWith.setEmail((String) userMap.get("email"));
                }
                System.out.println(chattingWith);
            } else {
                message = (String) response.get("message");
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while loading direct chat messages";
        }
    }

    public String sendDirectChatMessage() {
        if (messageContent == null && attachmentFile == null) {
            message = "Message content or attachment is required";
            return null;
        }

        try {
            WebTarget sendMessageTarget = base.path("directchats/send");

            HashMap<String, Object> requestBody = new HashMap<>();
            if (attachmentFile != null) {
                InputStream fileInputStream = attachmentFile.getInputStream();
                String filepath = sessionedUser.getUserUid() + "/media/" + attachmentFile.getSubmittedFileName();
                Files.createDirectories(Paths.get(filepath).getParent());
                Files.copy(fileInputStream, Paths.get(filepath), StandardCopyOption.REPLACE_EXISTING);
                requestBody.put("attachmentPath", filepath);
                requestBody.put("attachmentFilename", attachmentFile.getSubmittedFileName());
                System.out.println("Attachment uploaded to: " + filepath);
                System.out.println("Attachment filename: " + attachmentFile.getSubmittedFileName());
            }
            requestBody.put("content", messageContent);
            requestBody.put("conversationId", String.valueOf(dashboardNavigationState.getDirectChatId()));
            requestBody.put("user_uid", sessionedUser.getUserUid());

            HashMap<String, Object> response = sendMessageTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("success") != null) {
                messageContent = ""; // Clear input after successful send
                loadDirectChatMessages(); // Refresh chat log
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
            WebTarget deleteMessageTarget = base.path("directchats/delete");

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("message_id", String.valueOf(messageId));
            

            HashMap<String, Object> response = deleteMessageTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(requestBody), HashMap.class);

            if (response.get("message") != null) {
                messageContent = ""; // Clear input after successful send
                loadDirectChatMessages(); // Refresh chat log
            } else {
                message = (String) response.get("message");
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = "Error occurred while sending message";
        }

    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public HashMap<Integer, User> getUserDirectChats() {
        return userDirectChats;
    }

    public void setUserDirectChats(HashMap<Integer, User> userDirectChats) {
        this.userDirectChats = userDirectChats;
    }
    public List<DirectChatMessage> getDirectChatMessages() {
        return directChatMessages;
    }
    public void setDirectChatMessages(List<DirectChatMessage> directChatMessages) {
        this.directChatMessages = directChatMessages;
    }

    public User getChattingWith() {
        return chattingWith;
    }

    public void setChattingWith(User chattingWith) {
        this.chattingWith = chattingWith;
    }
   
    public User getSessionedUser() {
        return sessionedUser;
    }
    public void setSessionedUser(SessionedUser sessionedUser) {
        this.sessionedUser = sessionedUser;
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
    public String getUserToDM() {
        return userToDM;
    }
    public void setUserToDM(String userToDM) {
        this.userToDM = userToDM;
    }
}

