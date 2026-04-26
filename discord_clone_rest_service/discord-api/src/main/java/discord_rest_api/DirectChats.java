package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_rest_api.models.DirectChat;
import discord_rest_api.models.User;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;


@Path("directchats")
public class DirectChats {

    private User getUserFromUserUID(String user_uid) {
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

    @POST
    @Path("list")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> getDirectChats(HashMap<String, String> JSON) {
        List<DirectChat> directChats = new ArrayList<DirectChat>();
        HashMap<String, Object> response = new HashMap<>();
        User user = getUserFromUserUID(JSON.get("user_uid"));
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
}
