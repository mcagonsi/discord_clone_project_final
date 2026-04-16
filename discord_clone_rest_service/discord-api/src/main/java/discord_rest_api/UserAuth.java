package discord_rest_api;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import at.favre.lib.crypto.bcrypt.BCrypt;
import discord_rest_api.models.User;
import discord_rest_api.utils.DatabaseConnection;
import jakarta.ws.rs.*;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/auth")
public class UserAuth implements Serializable {

    private static final String ONLINE = "online";
    private static final String OFFLINE = "offline";
    private static final int BCRYPT_COST = 12; // bcrypt cost factor

    private String message;

    // Helper methods for database operations
    private User getUserbyEmail(String email) {
        User user = null;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM users WHERE email = ?;")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordBytes(rs.getBytes("password"));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    private User getUserbyUsername(String username) {
        User user = null;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM users WHERE username = ?;")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordBytes(rs.getBytes("password"));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    private boolean verifyPassword(String inputPassword, byte[] storedHash) throws UnsupportedEncodingException {
        BCrypt.Result result = BCrypt.verifyer().verify(inputPassword.getBytes("UTF-16"),
                storedHash);
        return result.verified;
    }

    private boolean updateToken(User user) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE users SET token = SHA2(RAND(), 256) WHERE id = ?;")) {
            stmt.setInt(1, user.getId());
            int rs = stmt.executeUpdate();
            return rs > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateStatus(User user, String status) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE users SET status = ? WHERE id = ?;")) {
            stmt.setString(1, status);
            stmt.setInt(2, user.getId());
            int rs = stmt.executeUpdate();
            return rs > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private User getLoggedinUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM users WHERE id = ?;")) {
            stmt.setInt(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User updatedUser = new User();
                    updatedUser.setId(rs.getInt("id"));
                    updatedUser.setUid(rs.getString("user_uid"));
                    updatedUser.setDisplay_name(rs.getString("display_name"));
                    updatedUser.setUsername(rs.getString("username"));
                    updatedUser.setEmail(rs.getString("email"));
                    updatedUser.setToken(rs.getString("token"));
                    updatedUser.setStatus(rs.getString("status"));
                    updatedUser.setCreated_at(rs.getString("created_at"));
                    return updatedUser;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // API endpoints
    @POST
    @Path("/signup")
    @Consumes("application/json")
    @Produces("application/json")
    public HashMap<String, Object> signup(User user) {
        HashMap<String, Object> response = new HashMap<>();

        System.out.println("Signup endpoint called for user: " + user);
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            System.out.println("Email is required for signup.");
            response.put("message", "Email is required.");
            return response;
        }
        if (!user.getEmail().contains("@")) {
            System.out.println("Invalid email format: " + user.getEmail());
            response.put("message", "Invalid email format.");
            return response;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            System.out.println("Password is required for signup.");
            response.put("message", "Password is required.");
            return response;
        }
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            System.out.println("Username is required for signup.");
            response.put("message", "Username is required.");
            return response;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO users (user_uid, display_name, username, email, password, token) VALUES (?, ?, ?, ?, ?,SHA2(RAND(), 256));")) {
            byte[] hashedPassword = BCrypt.withDefaults().hash(BCRYPT_COST, user.getPassword().getBytes("UTF-16"));
            user.generateUID();
            stmt.setString(1, user.getUid());
            stmt.setString(2, user.getDisplay_name());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, user.getEmail());
            stmt.setBytes(5, hashedPassword);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User signed up successfully: " + user.getUsername());
                message = "User signed up successfully.";
                response.put("message", message);

            } else {
                System.out.println("Failed to sign up user: " + user.getUsername());
                message = "Failed to sign up user.";
                response.put("message", message);

            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Username or email already exists.");

        }
        return response;
    }

    @POST
    @Path("/login")
    @Consumes("application/json")
    @Produces("application/json")
    public HashMap<String, Object> Login(User user) {
        HashMap<String, Object> response = new HashMap<>();
        // check if the input is an email or username
        User existingUser = null;
        if (user.getEmail() == null && user.getUsername() == null) {
            System.out.println("Email or username is required for login.");
            message = "Email or username is required for login.";
            response.put("message", message);
            return response;
        }
        if (user.getPassword() == null) {
            System.out.println("Password is required for login.");
            message = "Password is required for login.";
            response.put("message", message);
            return response;
        }
        if (user.getEmail() != null && !user.getEmail().contains("@")) {
            System.out.println("Invalid email format: " + user.getEmail());
            message = "Invalid email format.";
            response.put("message", message);
            return response;
        }
        if (user.getUsername() != null && user.getUsername().isEmpty()) {
            System.out.println("Username cannot be empty.");
            message = "Username cannot be empty.";
            response.put("message", message);
            return response;
        }
        if (user.getEmail() != null) {
            existingUser = getUserbyEmail(user.getEmail());
        } else if (user.getUsername() != null) {
            existingUser = getUserbyUsername(user.getUsername());
        }
        if (existingUser == null) {
            System.out.println("User not found: " + user.getEmail());
            message = "User not found.";
            response.put("message", message);
            return response;
        }
        try {
            if (verifyPassword(user.getPassword(), existingUser.getPasswordBytes())) {
                System.out.println("Login successful for user: " + user.getUsername());
                updateToken(existingUser);
                updateStatus(existingUser, ONLINE);
                User loggedInUser = getLoggedinUser(existingUser);
                response.put("user", loggedInUser);
                response.put("message", "Login successful.");
            } else {
                System.out.println("Invalid credentials for user: " + user.getUsername());
                message = "Invalid credentials.";
                response.put("message", message);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            message = "Something went wrong during login.";
            response.put("message", message);
        }
        return response;
    }

}
