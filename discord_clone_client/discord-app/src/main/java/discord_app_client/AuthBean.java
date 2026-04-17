package discord_app_client;

import java.io.Serializable;
import java.util.HashMap;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import discord_app_client.models.*;
import discord_app_client.utils.Variables;


@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {
    private String emailOrUsername;
    private String password;
    private String token;
    private String authType; // "login" or "register"
    private User user;
    private Client client;
    private WebTarget base;
    private String message;
    private String confirmPassword;

    @PostConstruct
    public void init() {
        if (user == null) {
            user = new User();
        }
        client = ClientBuilder.newClient();
        base = client.target(Variables.API_URL);
    }

    @PreDestroy
    public void cleanup() {
        if (client != null) {
            client.close();
        }
    }

    public void loginUser() {

        if (emailOrUsername == null || password == null) {
            System.out.println("Email/Username and password must not be null");
            message = "Email/Username and password are required.";
        }
        System.out.println("Attempting to log in with: " + emailOrUsername);
        // Set either email or username based on the input and also set the password
        if (emailOrUsername.contains("@")) {
            user.setEmail(emailOrUsername);
        } else {
            user.setUsername(emailOrUsername);
        }
        user.setPassword(password);

        // Implement login logic using base WebTarget
        try {
            HashMap<String, Object> response;
            WebTarget loginTarget = base.path("auth/login");
            response = loginTarget.request(MediaType.APPLICATION_JSON).post(Entity.json(user), HashMap.class);
            System.out.println("Login response: " + response);
            if (response.containsKey("user")) {
                message = response.get("message").toString();
                HashMap<String, Object> userMap = (HashMap<String, Object>) response.get("user");
                user.setUserUid(userMap.get("uid").toString());
                user.setCreated_at(userMap.get("created_at").toString());
                user.setDisplay_name(userMap.get("display_name").toString());
                user.setEmail(userMap.get("email").toString());
                user.setUsername(userMap.get("username").toString());
                user.setToken(userMap.get("token").toString());
                user.setStatus(userMap.get("status").toString());
                user.setPassword(null); // Clear password for security
                this.token = user.getToken();
                System.out.println("Login successful! User: " + user.getUsername() + ", Token: " + token);
            } else {
                message = response.get("message").toString();
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            message = "Login failed: " + e.getMessage();
        }

    }

    public void registerUser() {

        if(user.getDisplay_name() == null || user.getUsername() == null || user.getEmail() == null || user.getPassword() == null) {
            message = "All fields are required for registration.";
            
        }
        if(user.getPassword() != null && !user.getPassword().equals(confirmPassword)) {
            message = "Password and Confirm Password do not match.";
            
        }
            try {
                HashMap<String,Object> response = base.path("auth/signup").request(MediaType.APPLICATION_JSON).post(Entity.json(user), HashMap.class);
                System.out.println("Registration response: " + response);
                message = response.get("message").toString();
            } catch (Exception e) {
                System.out.println("Registration failed: " + e.getMessage());
                message = "Registration failed: " + e.getMessage();
            }
    }

    public String getEmailOrUsername() {
        return emailOrUsername;
    }

    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthType() {
        return authType;
    }
    
     public void setAuthType(String authType) {
        this.authType = authType;
    }

     public User getUser() {
        return user;
     }

     public void setUser(User user) {
        this.user = user;
     }


}
