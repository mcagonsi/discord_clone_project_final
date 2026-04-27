package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_rest_api.models.User;
import discord_rest_api.utils.CommonGetters;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("friends")
public class Friends {

    private Integer checkIfFriendsOrRequestExists(User user, User friend) {
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM friends WHERE (user_id=? AND friend_user_id=?) OR (user_id=? AND friend_user_id=?);")) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, friend.getId());
            stmt.setInt(3, friend.getId());
            stmt.setInt(4, user.getId());
            try (
                    ResultSet rs = stmt.executeQuery();) {
                if (rs.next()) {
                    return rs.getInt("id");
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
    public HashMap<String, Object> getFriendsList(HashMap<String, String> JSON) {
        List<User> friends = new ArrayList<User>();
        HashMap<String, Object> response = new HashMap<>();
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        if (user == null) {
            response.put("message", "Invalid user credentials");
            return response;
        }
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT CASE " +
                                "WHEN user_id = ? THEN friend_user_id " +
                                "ELSE user_id " +
                                "END AS friend_id " +
                                "FROM friends " +
                                "WHERE (user_id = ? OR friend_user_id = ?) AND accepted = 1")) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, user.getId());
            stmt.setInt(3, user.getId());
            try (
                    ResultSet rs = stmt.executeQuery();) {
                while (rs.next()) {
                    User friend = CommonGetters.getUserFromId(rs.getInt("friend_id"));

                    if (friend != null && !(friend.getId() == user.getId())) {
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
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        User friend = CommonGetters.getUserByUsername(JSON.get("friend"));

        if (user == null || friend == null) {
            response.put("message", "Invalid user credentials");
            return response;
        } else if (friend.getId() == user.getId()) {
            response.put("message", "Cannot send friend request to yourself");
            return response;
        } else {
            Integer existingRequest = checkIfFriendsOrRequestExists(user, friend);
            if (existingRequest != null) {
                response.put("message",
                        "Unable to send friend request, did you send one already? or are you already friends?");
                return response;
            }
            try (
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO friends (user_id, friend_user_id) VALUES (?, ?);")) {
                stmt.setInt(1, user.getId());
                stmt.setInt(2, friend.getId());

                stmt.execute();

                response.put("message", "Friend request sent!");
            } catch (SQLException e) {
                e.printStackTrace();
                response.put("message", "Failed to send friend request.");
            }
        }
        return response;
    }

    @POST
    @Path("acceptRequest")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> acceptFriendRequest(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User sender = CommonGetters.getUserFromUserUID(JSON.get("sender"));
        User receiver = CommonGetters.getUserFromUserUID(JSON.get("receiver"));

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM friends WHERE (user_id=? AND friend_user_id=?) OR (user_id=? AND friend_user_id=?);")) {
            stmt.setInt(1, sender.getId());
            stmt.setInt(2, receiver.getId());
            stmt.setInt(3, receiver.getId());
            stmt.setInt(4, sender.getId());
            try (
                    ResultSet rs = stmt.executeQuery();) {
                if (!rs.next()) {
                    response.put("message", "Friend request not found.");
                    return response;
                }
                int requestID = rs.getInt("id");
                try (
                        PreparedStatement stmt2 = conn.prepareStatement(
                                "UPDATE friends SET accepted=1 WHERE id=?")) {
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
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        List<User> requests = new ArrayList<User>();
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM friends WHERE friend_user_id=? AND accepted=0;")) {
            stmt.setInt(1, user.getId());
            try (
                    ResultSet rs = stmt.executeQuery();) {
                while (rs.next()) {
                    User invite = CommonGetters.getUserFromId(rs.getInt("user_id"));
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
