package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_rest_api.models.Role;
import discord_rest_api.models.Server;
import discord_rest_api.models.ServerMember;
import discord_rest_api.models.User;
import discord_rest_api.utils.CommonGetters;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("servermembers")
public class GetServerMembers {

    private static List<Role> getRolesForUser(User user, Server server) {
        List<Role> roles = new ArrayList<Role>();
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM server_member_roles WHERE server_member_id=?;"
            )
        ) {
            stmt.setInt(1, user.getId());
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                while (rs.next()) {
                    Role role = Roles.getRoleById(rs.getInt("role_id"));
                    roles.add(role);
                }
                if (user.getId() == server.getOwnerId()) {
                    Role ownerRole = new Role();
                    ownerRole.setName("Owner");
                    roles.add(ownerRole);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return roles;
    }

    @POST
    @Path("list")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> getServerMemberList(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        List<ServerMember> serverMembers = new ArrayList<ServerMember>();
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT user_id FROM server_members WHERE server_id=?;"
            )
        ) {
            stmt.setInt(1, Integer.parseInt(JSON.get("server_id")));
            try(
                ResultSet rs = stmt.executeQuery();
            ) {
                while (rs.next()) {
                    ServerMember member = new ServerMember();
                    User user = CommonGetters.getUserFromId(rs.getInt("user_id"));
                    Server server = CommonGetters.getServerById(Integer.parseInt(JSON.get("server_id")));
                    List<Role> roles = getRolesForUser(user, server);
                    member.setUser(user);
                    member.setRole(roles);
                    System.out.println(roles);
                    serverMembers.add(member);
                }
                response.put("servermembers", serverMembers);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.put("message","Unable to fetch server members");
        }
        return response;
    }
}
