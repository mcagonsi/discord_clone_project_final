package discord_rest_api;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

import discord_rest_api.models.Server;
import discord_rest_api.models.User;
import discord_rest_api.utils.DatabaseConnection;
import discord_rest_api.utils.InviteCodeGenerator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/servers")
public class ServerResource implements Serializable {

    private User getOwnerFromUid(String user_uid) {
        // Logic to retrieve the User object based on the provided user_uid
        // This could involve querying the database or an in-memory data structure
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT id,token FROM users WHERE user_uid = ?")) {
            stmt.setString(1, user_uid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setToken(rs.getString("token"));
                    // Set other user properties as needed
                    return user;
                } else {
                    // Handle case where user is not found
                    return null;
                }
            }
            // Query the database to find the user by user_uid
            // If found, return the User object
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return null; // Placeholder return statement
    }

    public boolean validateToken(User user, String token) {
        if (user == null || token == null) {
            return false;
        }
        return user.getToken().equals(token);
    }

    @POST
    @Path("/create")
    @Consumes("application/json")
    public void createServer(HashMap<String, Object> JSON) {
        /// Expecting user_uid, token, name, description, is_public(boolean) in the JSON
        /// payload
        // Logic to create a new server using the provided data
        String user_uid = (String) JSON.get("user_uid");
        String token = (String) JSON.get("token");
        User owner = getOwnerFromUid(user_uid);
        if (owner == null) {
            System.out.println("User not found for user_uid: " + user_uid);
            return;
        }
        if (!validateToken(owner, token)) {
            System.out.println("Invalid token for user_uid: " + user_uid);
            return;
        }
        if (!JSON.containsKey("name") || !JSON.containsKey("description") || !JSON.containsKey("is_public")) {
            System.out.println("Missing required fields for server creation");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO servers (name, description, owner_id, isPublic, invite_code) VALUES (?, ?, ?, ?, ?)")) {
            // Generate a unique invite code for the server
            String inviteCode = InviteCodeGenerator.generateCode(8);

            System.out.println(
                    "Creating server with name: " + JSON.get("name") + ", description: " + JSON.get("description")
                            + ", owner_id: " + owner.getId() + ", is_public: " + (Boolean) JSON.get("is_public")
                            + ", invite_code: " + inviteCode);
            stmt.setString(1, (String) JSON.get("name"));
            stmt.setString(2, (String) JSON.get("description"));
            stmt.setInt(3, owner.getId());
            stmt.setBoolean(4, (Boolean) JSON.get("is_public")); // Store as private if is_public is false
            stmt.setString(5, inviteCode);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Server created successfully!");
            } else {
                System.out.println("Failed to create server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @POST
    @Path("/search")
    @Consumes("application/json")
    public void searchPublicServers(HashMap<String, Object> JSON) {

        // Expecting "search" field in the JSON payload containing the search query
        String query = (String) JSON.get("search");

        if (query == null || query.trim().isEmpty()) {
            System.out.println("Search query cannot be null or empty");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM servers WHERE isPublic = true AND (name LIKE ? OR description LIKE ?)")) {
            String queryParam = "%" + query + "%";
            List<Server> foundServers = new java.util.ArrayList<>();
            stmt.setString(1, queryParam);
            stmt.setString(2, queryParam);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Server server = new Server();
                    server.setId(rs.getInt("id"));
                    server.setName(rs.getString("name"));
                    server.setDescription(rs.getString("description"));
                    server.setCreatedAt(rs.getTimestamp("created_at").toString());
                    server.setOwnerId(rs.getInt("owner_id"));
                    server.setPublic(rs.getBoolean("isPublic"));
                    foundServers.add(server);
                    System.out.println("Found public server - ID: " + server.getId() + ", Name: " + server.getName()
                            + ", Description: " + server.getDescription());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // need to do a trigger that if someone joins a server, it automatically assigns a role (everyone) and addes them to the general channel
        // the everyone role should automatically have read permissions for the general channel and write permissions for the general channel
        // we need to figure out how to make the invite code system work especially for joining private servers. should it be unique
        // we could for the invite link sent join the invitecode + serverid as one then it becomes a path param 
        // that we can slice first 8 characters for the invite code and the rest for the server id. then we can check if the invite code is valid for that server and if so, add the user to the server and assign them the everyone role
        // the link will take someone to a page that shows the server name, description, and an accept invite button. if they click the accept invite button, it will trigger the join server endpoint with the invite code and server id as path params. then we can check if the invite code is valid for that server and if so, add the user to the server and assign them the everyone role
        // also making sure the user is signed in and has a valid token before allowing them to join the server. if they are not signed in, we can redirect them to the login page and after they log in, we can redirect them back to the server invite page where they can click the accept invite button to join the server. this way we can ensure that only authenticated users can join servers and we can also track which user joined which server for future features like showing a list of servers a user is a member of on their profile page.

    }
}
