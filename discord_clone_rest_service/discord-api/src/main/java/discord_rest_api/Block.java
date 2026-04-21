package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_rest_api.models.User;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("block")
public class Block {

    //TODO: Copied from friends, maybe move from both classes into its (util?) own to reduce code duplication
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

    //TODO: see todo for above
    private User getUserFromId(int id) {
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE id=?;"
            );
        ) {
            stmt.setInt(1, id);
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setDisplay_name(rs.getString("display_name"));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //TODO: see todo for above (?)
    private User getUserbyUsername(String username) {
        User user = null;
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ?;"
            )
        ) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @POST
    @Path("list")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> getBlockList(HashMap<String, String> JSON) {
        List<User> blocks = new ArrayList<User>();
        HashMap<String, Object> response = new HashMap<>();
        User user = getUserIdFromUserUID(JSON.get("user_uid"));
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT blocked_user_id FROM blocked_users WHERE user_id=?;"
            )
        ) {
            stmt.setInt(1, user.getId());
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                while (rs.next()) {
                    User blockedUser = getUserFromId(rs.getInt("blocked_user_id"));
                    if (blockedUser != null) {
                        blocks.add(blockedUser);
                    }
                }
                response.put("blocklist", blocks);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message", "Failed to retrieve block list");
        }
        return response;
    }

    @POST
    @Path("user")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> blockUser(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = getUserIdFromUserUID(JSON.get("user_uid"));
        User userToBlock = getUserbyUsername(JSON.get("user_to_block"));

        if (user == null) {
            response.put("message", "Invalid user credentials");
        } else {
            if (userToBlock != null) {
                try (
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO blocked_users (user_id, blocked_user_id) VALUES (?, ?);"
                    )
                ) {
                    stmt.setInt(1, user.getId());
                    stmt.setInt(2, userToBlock.getId());

                    stmt.execute();

                    response.put("message", "User "+ JSON.get("user_to_block") +" was blocked successfully!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.put("message", "Failed to block user");
                }
            } else {
                response.put("message", "User with username "+ JSON.get("user_to_block") +" does not exist.");
            }
        }
        return response;
    }
}
