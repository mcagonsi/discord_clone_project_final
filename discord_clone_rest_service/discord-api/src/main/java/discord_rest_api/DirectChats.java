package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_rest_api.models.DirectChat;
import discord_rest_api.models.DirectMessage;
import discord_rest_api.models.DirectMsgAttachment;
import discord_rest_api.models.User;
import discord_rest_api.utils.CommonGetters;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;


@Path("directchats")
public class DirectChats {

    private DirectChat getDirectChatFromId(int id) {
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM direct_chats WHERE id=?;"
            );
        ) {
            stmt.setInt(1, id);
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                if (rs.next()) {
                    DirectChat dc = new DirectChat();
                    dc.setId(rs.getInt("id"));
                    dc.setSenderId(rs.getInt("sender_id"));
                    dc.setReceiverId(rs.getInt("receiver_id"));
                    return dc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DirectMsgAttachment getAttachmentFromMessageId(int id) {
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM direct_chat_msg_attachment WHERE direct_chat_msg_id=?;"
            )
        ) {
            stmt.setInt(1, id);
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                if (rs.next()) {
                    DirectMsgAttachment attachment = new DirectMsgAttachment();
                    attachment.setId(rs.getInt("id"));
                    attachment.setDirectChatMessageId(id);
                    attachment.setFilename(rs.getString("file_name"));
                    attachment.setPath(rs.getString("file_path"));
                    return attachment;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if the id is blocked by the current user
     * @param user current user
     * @param id id to check if blocked
     * @return true or false
     */
    private boolean checkIfBlocked(User user, int id) {
        boolean result = false;

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT blocked_user_id FROM blocked_users WHERE user_id=?;"
            );
        ) {
            stmt.setInt(1, user.getId());
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                while(rs.next()) {
                    if (rs.getInt("blocked_user_id") == id) {
                        result = true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();;
        }

        return result;
    }

    @POST
    @Path("list")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> getDirectChats(HashMap<String, String> JSON) {
        List<DirectChat> directChats = new ArrayList<DirectChat>();
        HashMap<String, Object> response = new HashMap<>();
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM direct_chats WHERE sender_id=? OR receiver_id=?;"
            );
        ) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, user.getId());

            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                
                while (rs.next()) {
                    DirectChat directChat = getDirectChatFromId(rs.getInt("id"));
                    if (directChat != null) {
                        directChats.add(directChat);
                    }
                }
                response.put("directChatList", directChats);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message", "Failed to retrieve direct chats");
        }
        return response;
    }

    //TODO: Attachments need to be tested, not sure how I would do such
    @POST
    @Path("chatlog")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> getDirectChatLog(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        DirectChat directChat = getDirectChatFromId(Integer.parseInt(JSON.get("directChatId")));
        User currentUser = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        List<DirectMessage> messages = new ArrayList<DirectMessage>();

        int otherUserId;
        if (currentUser.getId() == directChat.getSenderId()) {
            otherUserId = directChat.getReceiverId();
        } else {
            otherUserId = directChat.getSenderId();
        }
        User otherUser = CommonGetters.getUserFromId(otherUserId);
        response.put("other_user", otherUser);

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM direct_chat_messages WHERE conversation_id=? AND is_deleted=0;"
            );
        ) {
            stmt.setInt(1, directChat.getId());
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                while(rs.next()) {
                    DirectMessage message = new DirectMessage();
                    message.setAuthorId(rs.getInt("sender_user_id"));
                    message.setCreatedAt(rs.getString("created_at"));
                    message.setDirectChatId(directChat.getId());
                    message.setId(rs.getInt("id"));

                    if (!checkIfBlocked(currentUser, otherUserId)) {
                        message.setContent(rs.getString("content"));
                    } else {
                        message.setContent("<You blocked this user>");
                    }

                    DirectMsgAttachment attachment = getAttachmentFromMessageId(rs.getInt("id"));
                    if (attachment == null) {
                        message.setAttachment(attachment);
                    }

                    messages.add(message);
                }
                response.put("chatlog", messages);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message", "Failed to retrieve chatlog");
        }
        return response;
    }


    @POST
    @Path("send")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> sendDirectMessage(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO direct_chat_messages (conversation_id, sender_user_id, content) VALUES (?, ?, ?);"
            );
        ) {
            stmt.setInt(1, Integer.parseInt(JSON.get("conversationId")));
            stmt.setInt(2, user.getId());
            stmt.setString(3, JSON.get("content"));
            
            int result = stmt.executeUpdate();
            if (result == 1) {
                if(JSON.get("attachmentPath") != null && JSON.get("attachmentFilename") != null) {
                    try (
                        PreparedStatement attachmentStmt = conn.prepareStatement(
                            "INSERT INTO direct_chat_msg_attachment (direct_chat_msg_id, file_name, file_path) VALUES (LAST_INSERT_ID(), ?, ?);"
                        );
                    ) {
                        attachmentStmt.setString(1, JSON.get("attachmentFilename"));
                        attachmentStmt.setString(2, JSON.get("attachmentPath"));
                        attachmentStmt.executeUpdate();
                    }
                }
                response.put("success", "Message sent successfully");
            } else {
                response.put("message", "Could not send message");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message", "Could not send message");
        }
        return response;
    }

    @POST
    @Path("delete")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> deleteDirectMessage(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE direct_chat_messages SET is_deleted=1 WHERE id=?;"
            );
        ) {
            stmt.setInt(1, Integer.parseInt(JSON.get("direct_chat_message_id")));
            
            int result = stmt.executeUpdate();
            if (result == 1) {
                response.put("message", "Message deleted successfully");
            } else {
                response.put("message", "Could not delete message");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message", "Could not delete message");
        }
        return response;
    }

}
