package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import discord_rest_api.models.User;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("directmessage")
public class DirectMessages {

    private User getUserIdFromUserUID(String user_uid) {
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE user_uid=?;"
            )
        ) {
            stmt.setString(1, user_uid);
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordBytes(rs.getBytes("password"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // TODO: this currently requires the id for the direct chat, might need to be changed at some point
    // TODO: add attachments
    @POST
    @Path("send")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> sendDirectMessage(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = getUserIdFromUserUID(JSON.get("user_uid"));
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
                response.put("message", "Message sent successfully");
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
                "UPDATE direct_chat_messages SET is_deleted=1 WHERE ;" //TODO: finish this
            );
        ) {
            //TODO: finish this
            
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

    /* TODO: possibly add method to get all messages - with deleted items content replaced with notice of deletion */
}
