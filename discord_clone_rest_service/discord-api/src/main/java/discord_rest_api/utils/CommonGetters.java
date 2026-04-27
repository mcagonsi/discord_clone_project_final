package discord_rest_api.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import discord_rest_api.models.Server;
import discord_rest_api.models.User;

public class CommonGetters {

    /**
     * Gets a server by its database id
     * @param serverId sql id for server
     * @return Server object
     */
    public static Server getServerById(int serverId) {
        // Logic to retrieve the Server object based on the provided serverId
        // This could involve querying the database or an in-memory data structure
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id, name, description, owner_id, invite_code, isPublic FROM servers WHERE id = ?")) {
            stmt.setInt(1, serverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Server server = new Server();
                    server.setId(rs.getInt("id"));
                    server.setName(rs.getString("name"));
                    server.setDescription(rs.getString("description"));
                    server.setOwnerId(rs.getInt("owner_id"));
                    server.setInviteCode(rs.getString("invite_code"));
                    server.setPublic(rs.getBoolean("isPublic"));
                    // Set other server properties as needed
                    return server;
                } else {
                    // Handle case where server is not found
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return null; // Placeholder return statement
    }

    /**
     * Gets user from their user_id
     * @param user_uid user_id in sql database
     * @return User object
     */
    public static User getUserFromUserUID(String user_uid) {
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
                    user.setUid(user_uid);
                    user.setDisplay_name(rs.getString("display_name"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordBytes(rs.getBytes("password"));
                    user.setToken(rs.getString("token"));
                    user.setStatus(rs.getString("status"));
                    return user;
                } else {
                    // Handle case where user is not found
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets user from database id
     * @param id sql database id
     * @return User object
     */
    public static User getUserFromId(int id) {
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM users WHERE id=?;");) {
            stmt.setInt(1, id);
            try (
                    ResultSet rs = stmt.executeQuery();) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(id);
                    user.setUid(rs.getString("user_uid"));
                    user.setDisplay_name(rs.getString("display_name"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordBytes(rs.getBytes("password"));
                    user.setToken(rs.getString("token"));
                    user.setStatus(rs.getString("status"));
                    return user;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets user by their username
     * @param username username
     * @return User object
     */
    public static User getUserByUsername(String username) {
        User user = null;
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM users WHERE username = ?;")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUid(rs.getString("user_uid"));
                    user.setDisplay_name(rs.getString("display_name"));
                    user.setUsername(username);
                    user.setEmail(rs.getString("email"));
                    user.setPasswordBytes(rs.getBytes("password"));
                    user.setToken(rs.getString("token"));
                    user.setStatus(rs.getString("status"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

}
