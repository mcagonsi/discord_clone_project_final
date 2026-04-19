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

@Path("friends")
public class Friends {

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
    public HashMap<String, Object> getFriendsList(HashMap<String, String> JSON) {
        List<User> friends = new ArrayList<User>();
        HashMap<String, Object> response = new HashMap<>();
        User user = getUserIdFromUserUID(JSON.get("user_uid"));
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT friend_user_id FROM friends WHERE user_id=? AND accepted=1;"
            )
        ) {
            stmt.setInt(1, user.getId());
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                while (rs.next()) {
                    User friend = getUserFromId(rs.getInt("friend_user_id"));
                    if (friend != null) {
                        friends.add(friend);
                    }
                }
                response.put("friendslist", friends);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message", "Failed to retrieve friends list");
        }
        return response;
    }
    
    @POST
    @Path("sendRequest")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> sendFriendRequest(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = getUserIdFromUserUID(JSON.get("user_uid"));
        User friend = getUserbyUsername(JSON.get("friend"));

        if (user == null) {
            response.put("message", "Invalid user credentials");
        } else {
            if (friend != null) {
                try (
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO friends (user_id, friend_user_id) VALUES (?, ?);"
                    )
                ) {
                    stmt.setInt(1, user.getId());
                    stmt.setInt(2, friend.getId());

                    stmt.execute();

                    response.put("message", "Friend request sent!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.put("message", "Unable to send friend request, did you send one already?.");
                }
            } else {
                response.put("message", "User with username "+ JSON.get("friend") +" does not exist.");
            }
        }
        return response;
    }

    //TODO: Maybe make this go both ways?
    @POST
    @Path("acceptRequest")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> acceptFriendRequest(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User sender = getUserIdFromUserUID(JSON.get("sender"));
        User receiver = getUserIdFromUserUID(JSON.get("receiver"));

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM friends WHERE user_id=? AND friend_user_id=?;"
            )
        ) {
            stmt.setInt(1, sender.getId());
            stmt.setInt(2, receiver.getId());
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                if (!rs.next()) {
                    response.put("message", "Friend request not found.");
                    return response;
                }
                int requestID = rs.getInt("id");
                try (
                    PreparedStatement stmt2 = conn.prepareStatement(
                        "UPDATE friends SET accepted=1 WHERE id=?"
                    )
                ) {
                    stmt2.setInt(1, requestID);
                    stmt2.execute();
                    response.put("message", "Friend request accepted!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message", "Unable to accept friend request.");
        }
        return response;
    }

    @POST
    @Path("incomingRequests")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> viewIncomingRequests(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = getUserIdFromUserUID(JSON.get("user_uid"));
        List<User> requests = new ArrayList<User>();
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM friends WHERE friend_user_id=?;"
            )
        ) {
            stmt.setInt(1, user.getId());
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                while (rs.next()) {
                    User invite = getUserFromId(rs.getInt("user_id"));
                    if (invite != null) {
                        requests.add(invite);
                    }
                }
                response.put("requests", requests);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

}
