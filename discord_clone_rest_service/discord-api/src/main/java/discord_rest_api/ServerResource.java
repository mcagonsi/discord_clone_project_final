package discord_rest_api;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_rest_api.models.DirectChat;
import discord_rest_api.models.Permission;
import discord_rest_api.models.Role;
import discord_rest_api.models.Server;
import discord_rest_api.models.User;
import discord_rest_api.utils.CommonGetters;
import discord_rest_api.utils.ConstantVariables;
import discord_rest_api.utils.DatabaseConnection;
import discord_rest_api.utils.InviteCodeGenerator;
import discord_rest_api.utils.PermissionConst;
import jakarta.ws.rs.*;

@Path("/servers")
public class ServerResource implements Serializable {

    private boolean sendInviteDirectMessage(User sender, User receiver, String serverId) {
        // robust serverId parse
        Server server = CommonGetters.getServerById(Integer.parseInt(serverId));
        int conversation_id = DirectChat.checkOrCreateConversationIdForUsers(sender, receiver);
         // send message if we have a conversation id
        if (conversation_id != -1) {
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement msgStmt = conn.prepareStatement(
                            "INSERT INTO direct_chat_messages (conversation_id, sender_user_id, content) VALUES (?, ?, ?)");) {
                msgStmt.setInt(1, conversation_id);
                msgStmt.setInt(2, sender.getId());
                msgStmt.setString(3, "Server Invite. Server Name: " + server.getName()
                        + ", Invite Code: " + server.getInviteCode() + ", Link: " + ConstantVariables.DOMAIN_URL + "/"
                        + server.getId() + "/" + server.getInviteCode());
                int msgRowsAffected = msgStmt.executeUpdate();
                if (msgRowsAffected > 0) {
                    System.out.println("Direct chat message sent successfully!");
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean checkInvitePermissions(User user, Server server) { //TODO: replace with the new partsed out one
        boolean hasPermission = false;
        if (user != null && server != null) {
            // Example logic: check if the user is the owner of the server
            if (user.getId() == server.getOwnerId()) {
                return true;
            }

            int servermemberId = -1;
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn
                            .prepareStatement("SELECT id FROM server_members WHERE user_id = ? AND server_id = ?")) {
                stmt.setInt(1, user.getId());
                stmt.setInt(2, server.getId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    // Found the user as a member of the server
                    System.out.println("User found as member of the server" + rs.getInt("id"));
                    servermemberId = rs.getInt("id");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (servermemberId != -1) {
                List<Role> userRoles = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(
                                "SELECT r.id, r.name FROM roles r JOIN server_member_roles smr ON r.id = smr.role_id WHERE smr.server_member_id = ?")) {
                    stmt.setInt(1, servermemberId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {

                        Role role = new Role();
                        role.setId(rs.getInt("id"));
                        role.setName(rs.getString("name"));

                        try (PreparedStatement smrStmt = conn.prepareStatement(
                                "SELECT p.id, p.name FROM permissions p JOIN role_permissions rp ON p.id = rp.permission_id WHERE rp.role_id = ?")) {
                            smrStmt.setInt(1, role.getId());
                            ResultSet permRs = smrStmt.executeQuery();
                            while (permRs.next()) {
                                Permission permission = new Permission();
                                permission.setId(permRs.getInt("id"));
                                permission.setName(permRs.getString("name"));
                                role.permissions.add(permission);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        userRoles.add(role);
                    }

                    for (Role role : userRoles) {
                        System.out.println("User has role: " + role.getName());
                        for (Permission permission : role.permissions) {
                            System.out.println(" - " + permission.getName());
                            if (permission.getName().equals(PermissionConst.INVITE_USER)) {
                                hasPermission = true;
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    hasPermission = false;
                }
            }

        }

        return hasPermission; // Return the permission status
    }

    @POST
    @Path("/create")
    @Consumes("application/json")
    @Produces("application/json")
    public HashMap<String, Object> createServer(HashMap<String, Object> request) {
        /// Expecting user_uid, token, name, description, is_public(boolean) in the JSON
        /// payload
        // Logic to create a new server using the provided data
        HashMap<String, Object> response = new HashMap<>();
        String user_uid = (String) request.get("user_uid");
        String token = (String) request.get("token");
        User owner = CommonGetters.getUserFromUserUID(user_uid);
        if (owner == null) {
            System.out.println("User not found for user_uid: " + user_uid);
            response.put("message", "User not found or bad credentials");
            return response;
        }
        if (!User.isValidUser(owner, token)) {
            System.out.println("Invalid token for user_uid: " + user_uid);
            response.put("message", "Invalid token");
            return response;
        }
        if (!request.containsKey("name") || !request.containsKey("description") || !request.containsKey("is_public")) {
            System.out.println("Missing required fields for server creation");
            response.put("message", "Missing required fields");
            return response;
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO servers (name, description, owner_id, isPublic, invite_code) VALUES (?, ?, ?, ?, ?)")) {
            // Generate a unique invite code for the server
            String inviteCode = InviteCodeGenerator.generateCode(8);

            System.out.println(
                    "Creating server with name: " + request.get("name") + ", description: " + request.get("description")
                            + ", owner_id: " + owner.getId() + ", is_public: " + (Boolean) request.get("is_public")
                            + ", invite_code: " + inviteCode);
            stmt.setString(1, (String) request.get("name"));
            stmt.setString(2, (String) request.get("description"));
            stmt.setInt(3, owner.getId());
            stmt.setBoolean(4, (Boolean) request.get("is_public")); // Store as private if is_public is false
            stmt.setString(5, inviteCode);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Server created successfully!");
                response.put("message", "Server created successfully!");
            } else {
                System.out.println("Failed to create server.");
                response.put("message", "Failed to create server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "An error occurred while creating the server.");
        }
        return response;
    }

    @POST
    @Path("/search")
    @Consumes("application/json")
    @Produces("application/json")
    public HashMap<String, Object> searchPublicServers(HashMap<String, Object> request) {
        HashMap<String, Object> response = new HashMap<>();

        // Expecting "search" field in the JSON payload containing the search query
        String query = (String) request.get("search");
        if (query == null || query.trim().isEmpty()) {
            System.out.println("Search query cannot be null or empty");
            response.put("message", "Search query cannot be null or empty");
            return response;
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
                    server.setInviteCode(rs.getString("invite_code"));
                    foundServers.add(server);
                    System.out.println("Found public server - ID: " + server.getId() + ", Name: " + server.getName()
                            + ", Description: " + server.getDescription());
                }
                response.put("servers", foundServers);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "An error occurred while searching for servers.");
        }
        return response;
    }

    @POST
    @Path("/invite")
    @Consumes("application/json")
    @Produces("application/json")
    public HashMap<String, Object> sendServerInvite(HashMap<String, Object> request) {
        HashMap<String, Object> response = new HashMap<>();
        // payload comes with serverid, invitedUser, and invitedBy, the invitedBy comes
        // with uid and token
        String serverId = (String) request.get("serverId");
        String invitedUsername = (String) request.get("invitedUser");
        HashMap<String, Object> invitedBy = (HashMap<String, Object>) request.get("invitedBy");
        String invitedByUid = (String) invitedBy.get("uid");
        String invitedByToken = (String) invitedBy.get("token"); // use this to validate user

        User invitedUser = CommonGetters.getUserByUsername(invitedUsername);
        User invitedByUser = CommonGetters.getUserFromUserUID(invitedByUid);

        Server server = CommonGetters.getServerById(Integer.parseInt(serverId));

        if (invitedByUser == null) {
            response.put("message", "Invalid user credentials.");
            return response;
        }
        if (!User.isValidUser(invitedByUser, invitedByToken)) {
            response.put("message", "Invalid token for user.");
            return response;
        }

        if (invitedUser == null) {
            response.put("message", "Invited user not found.");
            return response;
        }

        if (invitedUser.getUsername().equals(invitedByUser.getUsername())) {
            response.put("message", "You cannot invite yourself to a server.");
            return response;
        }

        boolean hasInvitePermission = checkInvitePermissions(invitedByUser, server);
        System.out.println("Has invite permission: " + hasInvitePermission);
        if (!hasInvitePermission) {
            response.put("message", "You do not have permission to invite users to this server.");
            return response;
        }

        if (hasInvitePermission && invitedByUser != null && invitedUser != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO server_invites (server_id, invited_user_id, invited_by_user_id) VALUES (?,?, ?)")) {
                stmt.setInt(1, Integer.parseInt(serverId));
                stmt.setInt(2, invitedUser.getId());
                stmt.setInt(3, invitedByUser.getId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    response.put("message", "Invite sent successfully.");
                    boolean invitemsgsent = sendInviteDirectMessage(invitedUser, invitedByUser, serverId);
                } else {
                    response.put("message", "Failed to send server invite. Have you sent an invite before?");
                }
            } catch (Exception e) {
                response.put("message", "Failed to send server invite. Have you sent an invite before?");
            }
        }
        return response;
    }

    @PUT
    @Path("/join/")
    @Consumes("application/json")
    @Produces("application/json")
    public HashMap<String, Object> joinServer(HashMap<String, Object> request) {
        HashMap<String, Object> response = new HashMap<>();
        int serverId = Integer.parseInt((String) request.get("serverId"));
        String inviteCode = (String) request.get("inviteCode");
        HashMap<String, Object> user = (HashMap<String, Object>) request.get("user");

        Server server = CommonGetters.getServerById(serverId);
        User userObj = CommonGetters.getUserFromUserUID((String) user.get("uid"));

        boolean userIsvalid = User.isValidUser(userObj, (String) user.get("token"));
        boolean userIsOnServer = false;
        boolean userHasInvite = false;

        if (userObj == null || !userIsvalid) {
            System.out.println("Invalid user or token for user.");
            response.put("message", "Invalid user or token.");
            return response;

        }
        if (inviteCode == null || inviteCode.trim().isEmpty() || serverId == 0 || server == null) {
            System.out.println("Invalid server credentials.");
            response.put("message", "Invalid server credentials.");
            return response;
        }


        boolean isServerPublic = server.isPublic();
        boolean inviteCodeIsValid = server.getInviteCode().equals(inviteCode) && server != null;
        if (inviteCode != null && !inviteCodeIsValid) {
            System.out.println("Invalid invite code.");
            response.put("message", "Invalid invite code.");
            return response;
        }
        // check if he exists on server
        if (inviteCode != null && inviteCodeIsValid) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                try (PreparedStatement stmt = conn
                        .prepareStatement("SELECT * FROM server_members WHERE server_id = ? AND user_id = ?")) {
                    stmt.setInt(1, serverId);
                    stmt.setInt(2, userObj.getId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        userIsOnServer = rs.next();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error occurred while checking if user is on the server.");
                response.put("message", "Error occurred while checking server membership.");
                return response;
            }
            if (userIsOnServer) {
                System.out.println("User is already a member of the server.");
                response.put("message", "You are already a member of the server.");
                return response;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // checking if user has an invite
            boolean hasAcceptedInvite = false;
            int inviteServerid = -1;
            if (!isServerPublic) {
                try (PreparedStatement stmt = conn
                        .prepareStatement("SELECT * FROM server_invites WHERE server_id = ? AND invited_user_id = ?")) {
                    stmt.setInt(1, serverId);
                    stmt.setInt(2, userObj.getId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        userHasInvite = rs.next();
                        if (userHasInvite) {
                            inviteServerid = rs.getInt("id");
                        }
                    }
                }
                if (inviteServerid != -1) {
                    try (PreparedStatement stmt = conn
                            .prepareStatement("UPDATE server_invites SET accepted = true WHERE id = ?")) {
                        stmt.setInt(1, inviteServerid);
                        int result = stmt.executeUpdate();
                        if (result > 0) {
                            hasAcceptedInvite = true;
                        }
                    }
                } else {
                    // Handle the case where the user does not have a valid invite
                    System.out.println("User does not have a valid invite for the server.");
                    response.put("message", "You do not have a valid invite for the server.");
                    return response;
                }
            }

            if (isServerPublic && inviteCodeIsValid || !isServerPublic && userHasInvite && hasAcceptedInvite) {
                // Proceed to join the server
                try (PreparedStatement stmt = conn
                        .prepareStatement("INSERT INTO server_members (server_id, user_id) VALUES (?, ?)")) {
                    stmt.setInt(1, server.getId());
                    stmt.setInt(2, userObj.getId());
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        System.out.println("User successfully added to the server.");
                        response.put("message", "You have successfully joined the server.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred while joining the server.");
            response.put("message", "Error occurred while joining the server.");
        }
        return response;
    }

}
