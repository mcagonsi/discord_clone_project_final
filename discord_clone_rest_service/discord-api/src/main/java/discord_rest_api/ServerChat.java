package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_rest_api.models.ServerMsgAttachment;
import discord_rest_api.models.MsgAttachment;
import discord_rest_api.models.ServerMessage;
import discord_rest_api.models.User;
import discord_rest_api.utils.CommonGetters;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("serverchats")
public class ServerChat {

    private ServerMsgAttachment getAttachmentFromMessageId(int id) {
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
                    ServerMsgAttachment attachment = new ServerMsgAttachment();
                    attachment.setId(rs.getInt("id"));
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
    @Path("send")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> sendServerMessage(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO server_messages (channel_id, user_id, content) VALUES (?, ?, ?);"
            );
        ) {
            stmt.setInt(1, Integer.parseInt(JSON.get("channelId")));
            stmt.setInt(2, user.getId());
            stmt.setString(3, JSON.get("content"));
            
            int result = stmt.executeUpdate();
            if (result == 1) {
                if(JSON.get("attachmentPath") != null && JSON.get("attachmentFilename") != null) {
                    try (
                        PreparedStatement attachmentStmt = conn.prepareStatement(
                            "INSERT INTO server_message_attachment (message_id, file_name, file_path) VALUES (LAST_INSERT_ID(), ?, ?);"
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
    @Path("chatlog")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> getDirectChatLog(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User currentUser = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        List<ServerMessage> messages = new ArrayList<ServerMessage>();

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM server_messages WHERE channel_id=? AND is_deleted=0;"
            );
        ) {
            stmt.setInt(1, Integer.parseInt(JSON.get("channel_id")));
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                while(rs.next()) {
                    ServerMessage message = new ServerMessage();
                    message.setAuthorId(rs.getInt("user_id"));
                    message.setChannelId(rs.getInt("channel_id"));
                    message.setCreatedAt(rs.getString("created_at"));
                    message.setId(rs.getInt("id"));

                    if (!checkIfBlocked(currentUser, rs.getInt("user_id"))) {
                        message.setContent(rs.getString("content"));
                    } else {
                        message.setContent("<You blocked this user>");
                    }

                    MsgAttachment attachment = getAttachmentFromMessageId(rs.getInt("id"));
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
    @Path("delete")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> deleteServerMessage(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE server_messages SET is_deleted=1 WHERE id=?;"
            );
        ) {
            stmt.setInt(1, Integer.parseInt(JSON.get("server_message_id")));
            
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
