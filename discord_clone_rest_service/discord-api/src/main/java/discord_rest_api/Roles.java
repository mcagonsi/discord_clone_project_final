package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import discord_rest_api.models.Role;
import discord_rest_api.models.Server;
import discord_rest_api.models.User;
import discord_rest_api.utils.CommonGetters;
import discord_rest_api.utils.DatabaseConnection;
import discord_rest_api.utils.PermissionConst;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("roles")
public class Roles {

    private Role getRoleById(int id) {
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM "
            )
        ) {
            
        } catch (SQLException e) {
            // TODO: handle exception
        }
        
        Role role = new Role();
        return null;
    }

    @POST
    @Path("create")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> createNewRole(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        Server server = CommonGetters.getServerById(Integer.parseInt(JSON.get("server_id")));
        String roleName = JSON.get("role_name");
        if (roleName == null || roleName == "") {
            response.put("message", "Please provide a role name");
        } else {
            if (CheckPermission.checkPermissionByName(user, server, PermissionConst.MANAGE_ROLES) && server != null) {
                try (
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO roles (server_id, name, created_by) VALUES (?, ?, ?);"
                    )
                ) {
                    stmt.setInt(1, server.getId());
                    stmt.setString(2, roleName);
                    stmt.setInt(3, user.getId());

                    int result = stmt.executeUpdate();
                    if (result == 1) {
                        response.put("message", "Role '" + roleName + "' created successfully");
                    } else {
                        response.put("message", "Failed to create role");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    response.put("message", "Failed to create role");
                }
            } else {
                if (server != null) {
                    response.put("message", user.getDisplay_name() + " does not have permission to manage roles in " + server.getName());
                } else {
                    response.put("message", "Could not find server");
                }
            }
        }
        return response;
    }

    @POST
    @Path("manage")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> alterRolePermissions(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        Server server = CommonGetters.getServerById(Integer.parseInt(JSON.get("server_id")));
        int roleId = Integer.parseInt(JSON.get("role_id"));
        int permissionId = Integer.parseInt(JSON.get("permission_id"));

        

        return response;
    }
}
