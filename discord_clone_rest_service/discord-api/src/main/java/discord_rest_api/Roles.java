package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import discord_rest_api.models.Permission;
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

    public static Role getRoleById(int id) {
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM roles WHERE id=?;"
            )
        ) {
            stmt.setInt(1, id);
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                if (rs.next()) {
                    Role role = new Role();
                    role.setId(id);
                    role.setServerId(rs.getInt("server_id"));
                    role.setName(rs.getString("name"));
                    return role;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Permission getPermissionById(int id) {
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM permissions WHERE id=?;"
            )
        ) {
            stmt.setInt(1, id);
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                if (rs.next()) {
                    Permission perm = new Permission();
                    perm.setId(id);
                    perm.setName(rs.getString("name"));
                    return perm;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    @Path("givepermission")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> givePermissionToRole(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        Server server = CommonGetters.getServerById(Integer.parseInt(JSON.get("server_id")));
        Role role = getRoleById(Integer.parseInt(JSON.get("role_id")));
        Permission permission = getPermissionById(Integer.parseInt(JSON.get("permission_id")));

        if (user == null) {
            response.put("message", "Invalid user credentials");
        } else if (server == null) {
            response.put("message", "Could not find server");
        } else if (role == null) {
            response.put("message", "Invalid role id");
        } else if (permission == null) {
            response.put("message", "Invalid permission id");
        } else {
            if (CheckPermission.checkPermissionByName(user, server, PermissionConst.MANAGE_ROLES) && server != null) {
                try (
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?);"
                    )
                ) {
                    stmt.setInt(1, role.getId());
                    stmt.setInt(2, permission.getId());

                    int result = stmt.executeUpdate();
                    if (result == 1) {
                        response.put("message", "'" +role.getName() + "' now has the " + permission.getName() + " permission");
                    } else {
                        response.put("message", "Failed to update role");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    response.put("message", "Failed to update role, does it have this permission already?");
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
    @Path("assign")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> assignRole(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User currentUser = CommonGetters.getUserFromUserUID(JSON.get("current_user_uid"));
        User userToAssignRole = CommonGetters.getUserFromUserUID(JSON.get("user_to_assign_role_uid"));
        Role role = getRoleById(Integer.parseInt(JSON.get("role_id")));
        Server server = CommonGetters.getServerById(Integer.parseInt(JSON.get("server_id")));        

        if (currentUser == null || userToAssignRole == null) {
            response.put("message", "Invalid user credentials");
        } else if (server == null) {
            response.put("message", "Could not find server");
        } else if (role == null) {
            response.put("message", "Invalid role id");
        } else {
            if (CheckPermission.checkPermissionByName(currentUser, server, PermissionConst.MANAGE_ROLES) && server != null) {
                try (
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO server_member_roles (server_member_id, role_id) VALUES (?, ?);"
                    )
                ) {
                    stmt.setInt(1, userToAssignRole.getId());
                    stmt.setInt(2, role.getId());

                    int result = stmt.executeUpdate();
                    if (result == 1) {
                        response.put("message", "User '"+userToAssignRole.getDisplay_name()+"' now has the role: "+role.getName());
                    } else {
                        response.put("message", "Unable to assign role");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.put("message", "Unable to assign role");
                }
            } else {
                if (server != null) {
                    response.put("message", currentUser.getDisplay_name() + " does not have permission to manage roles in " + server.getName());
                } else {
                    response.put("message", "Could not find server");
                }
            }
        }
        return response;
    }
}
