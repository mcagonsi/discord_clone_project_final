package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import discord_rest_api.models.User;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;


@Path("directchats")
public class DirectChats {

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

    //TODO: Complete this
    @POST
    @Path("list")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> getDirectChats(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = getUserIdFromUserUID(JSON.get("user_uid"));
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM direct_chats WHERE " // TODO: finish this statement
            );
        ) {
            
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
