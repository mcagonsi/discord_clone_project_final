package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import discord_rest_api.models.Permission;
import discord_rest_api.models.Role;
import discord_rest_api.models.Server;
import discord_rest_api.models.User;
import discord_rest_api.utils.DatabaseConnection;

public class CheckPermission {

    public static boolean checkPermissionByName(User user, Server server, String permissionName) {
        boolean hasPermission = false;
        if (user != null && server != null) {
            // Example logic: check if the user is the owner of the server
            if (user.getId() == server.getOwnerId()) {
                hasPermission = true;
            } else {
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
                        try(
                            ResultSet rs = stmt.executeQuery();
                        ) {
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
                        }

                        for (Role role : userRoles) {
                            System.out.println("User has role: " + role.getName());
                            for (Permission permission : role.permissions) {
                                System.out.println(" - " + permission.getName());
                                if (permission.getName().equals(permissionName)) {
                                    hasPermission = true;
                                }
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        hasPermission = false;
                    }
                } else {
                    System.out.println("Failed to retrieve server member id, is the user a server member?");
                }
            }
        }
        return hasPermission; // Return the permission status
    }
}
