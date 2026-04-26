package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import discord_rest_api.models.Server;
import discord_rest_api.models.User;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("channel")
public class Channel {

    // private Server getServerFromName(String name) {
    //     Server server = null;
    //     try (
    //         Connection conn = DatabaseConnection.getConnection();
    //         PreparedStatement stmt = conn.prepareStatement(
    //             "SELECT * FROM servers WHERE name"
    //         );
    //     ) {
            
    //     } catch (SQLException e) {
    //          e.printStackTrace();
    //     }
    //     return server;
    // }

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

    //TODO: should it also check if creator has permissions?
    //TODO: should it get server from id or another value (like name)?
    @POST
    @Path("create")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> createChannel(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        //Server server = getServerFromName(JSON.get("server_name")); //Unused as server name is not unique
        User user = getUserFromUserUID(JSON.get("user_uid"));
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO channels (server_id, name, created_by) VALUES (?, ?, ?);"
            );
        ) {
            stmt.setInt(1, Integer.parseInt(JSON.get("server_id")));
            stmt.setString(2, JSON.get("channel_name"));
            stmt.setInt(3, user.getId());

            int result = stmt.executeUpdate();

            if (result == 1) {
                response.put("message", "Channel '" + JSON.get("channel_name") + "' created successfully");
            } else {
                response.put("message", "Failed to create channel");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message", "Failed to create channel");
        }
        return response;
    }
}
