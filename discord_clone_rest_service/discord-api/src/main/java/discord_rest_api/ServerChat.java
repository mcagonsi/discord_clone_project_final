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
import discord_rest_api.models.MsgAttachment;
import discord_rest_api.models.User;
import discord_rest_api.utils.CommonGetters;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("serverchats")
public class ServerChat {

    private MsgAttachment getAttachmentFromMessageId(int id) {
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
                    MsgAttachment attachment = new MsgAttachment();
                    //TODO: change
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
