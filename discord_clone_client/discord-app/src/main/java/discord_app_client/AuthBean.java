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
    private String authType = "login"; // Can be "login" or "signup"
    private User user;
    private User loggedUser;
    private Client client;
    private WebTarget base;
    private String message;
    private String confirmPassword;
    private boolean isLoggedIn = false;

    @PostConstruct
    public void init() {
        if (user == null) {
            user = new User();
            loggedUser = new User();
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

    public boolean isLoggedIn() {
        this.isLoggedIn = (token != null && loggedUser != null && loggedUser.getToken() != null);
        return this.isLoggedIn;
    }

    public String checkLoginStatus() {
        if (isLoggedIn()) {
            System.out.println("User is logged in: " + loggedUser.getUsername());
            return "redirect:/user/user.xhtml?faces-redirect=true";
        }
        return null;
    }

    public String loginUser() {
        String redirect = null;
        if (emailOrUsername == null || password == null) {
            System.out.println("Email/Username and password must not be null");
            message = "Email/Username and password are required.";
            redirect = null;
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
                
                HashMap<String, Object> userMap = (HashMap<String, Object>) response.get("user");
                loggedUser.setUserUid(userMap.get("uid").toString());
                loggedUser.setCreated_at(userMap.get("created_at").toString());
                loggedUser.setDisplay_name(userMap.get("display_name").toString());
                loggedUser.setEmail(userMap.get("email").toString());
                loggedUser.setUsername(userMap.get("username").toString());
                loggedUser.setToken(userMap.get("token").toString());
                loggedUser.setStatus(userMap.get("status").toString());
                loggedUser.setPassword(null); // Clear password for security
                this.token = loggedUser.getToken();
                System.out.println("Login successful! User: " + loggedUser.getUsername() + ", Token: " + token);
                System.out.println(response.get("message").toString());
                message = null; // Clear any previous messages on successful login
                redirect = "redirect:../../user/index.xhtml?faces-redirect=true";
            } else {
                message = response.get("message").toString();
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            message = "Login failed: " + e.getMessage();
            redirect = null;
            
        }

       return redirect;
    }

    public void registerUser() {
       
        if (user.getDisplay_name() == null || user.getUsername() == null || user.getEmail() == null
                || user.getPassword() == null) {
            message = "All fields are required for registration.";
            user = new User(); // Reset user to clear any partial data
            return; 
        }
        if (user.getDisplay_name().isEmpty() || user.getUsername().isEmpty() || user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
            message = "All fields must be filled out for registration.";
            user = new User(); // Reset user to clear any partial data
            return;
        }
        if (user.getPassword() != null && !user.getPassword().equals(confirmPassword)) {
            message = "Password and Confirm Password do not match.";
            user = new User(); // Reset user to clear any partial data
            return;
        }
        try {
            HashMap<String, Object> response = base.path("auth/signup").request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(user), HashMap.class);
            System.out.println("Registration response: " + response);
            message = response.get("message").toString();
            authType = "login"; // Switch to login view after successful registration
            
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
            message = "Registration failed: " + e.getMessage();
            return;
            
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

    public void showLogin() {
        this.authType = "login";
        this.message = null; // Clear any previous messages
    }

    public void showSignup() {
        this.authType = "signup";
        this.message = null; // Clear any previous messages
    }

    // for now 
    public User getLoggedUser() {
        return loggedUser;
    }
   

}
