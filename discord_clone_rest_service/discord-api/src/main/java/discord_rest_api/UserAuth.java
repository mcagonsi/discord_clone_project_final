package discord_rest_api;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;

import discord_rest_api.models.User;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/auth")
public class UserAuth implements Serializable {
    private User user;

    private User getUserbyEmail(String email) {
        return user;
    }

    private User getUserbyUsername(String username) {
        return user;
    }

    private boolean verifyPassword(String password) {
        return true;
    }

    @POST
    @Path("/signup")
    @Consumes("application/json")
    public void signup(User user) {
        System.out.println("Signup endpoint called for user: " + user);
        user.generateUID();
        System.out.println("User UID: " + user.getUid());
        System.out.println("User email: " + user.getEmail());

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO users (user_uid, display_name, username, email, password, token) VALUES (?, ?, ?, ?, ?,SHA2(RAND(), 256));")) {
            stmt.setString(1, user.getUid());
            stmt.setString(2, user.getDisplay_name());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPassword());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User signed up successfully: " + user.getUsername());
            } else {
                System.out.println("Failed to sign up user: " + user.getUsername());
            }
            // note i need to update the user login status to defualt online on the database sql file, and change the password type to binary like
            // the class examples. ensure to hash passwords.
            // edge cases to check if all the required fields are present before proceeding with signup

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}
