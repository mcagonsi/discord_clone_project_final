package discord_app_client;

import java.io.Serializable;
import java.util.HashMap;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import discord_app_client.models.*;
import discord_app_client.utils.Variables;
import jakarta.inject.Inject;

@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {
    @Inject
    private SessionedUser loggedUser;
    private String emailOrUsername;
    private String authType; // Can be "login" or "signup"
    private User user;
    private Client client;
    private WebTarget base;
    private String message;
    private String confirmPassword;
    private boolean isLoggedIn = false;

    @PostConstruct
    public void init() {
        if (user == null) {
            user = new User();
            authType = "login";
            // loggedUser = new SessionedUser();
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
        this.isLoggedIn = (loggedUser != null && loggedUser.getToken() != null);
        return this.isLoggedIn;
    }

    public String checkLoginStatus() {
        if (isLoggedIn()) {
            System.out.println("User is logged in: " + loggedUser.getUsername());
            return "/user/index.xhtml?faces-redirect=true";
        }
        return null;
    }

    private void resetFields() {
        user = new User();
        emailOrUsername = null;
        confirmPassword = null;
    }

    public String loginUser() {
        String redirect = null;
        if (emailOrUsername == null || user.getPassword() == null) {
            System.out.println("Email/Username and password must not be null");
            message = "Email/Username and password are required.";

            resetFields();
        }
        System.out.println("Attempting to log in with: " + emailOrUsername);
        // Set either email or username based on the input and also set the password
        if (emailOrUsername.contains("@")) {
            user.setEmail(emailOrUsername);
        } else {
            user.setUsername(emailOrUsername);
        }

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
                System.out.println(
                        "Login successful! User: " + loggedUser.getUsername() + ", Token: " + loggedUser.getToken());
                System.out.println(response.get("message").toString());
                message = null; // Clear any previous messages on successful login
                redirect = "/user/index.xhtml?faces-redirect=true";

            } else {
                message = response.get("message").toString();
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            message = "Login failed: " + e.getMessage();
            redirect = null;

        } finally {
            resetFields();
        }

        return redirect;
    }

    public String registerUser() {
        System.out.println("registerUser() called with user=" + user);

        // validation...
        if (user == null
                || user.getDisplay_name() == null || user.getUsername() == null
                || user.getEmail() == null || user.getPassword() == null
                || user.getDisplay_name().isEmpty() || user.getUsername().isEmpty()
                || user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
            message = "All fields are required for registration.";
            return null; // stay and show message
        }

        if (confirmPassword == null || !confirmPassword.equals(user.getPassword())) {
            message = "Passwords do not match.";
            return null;
        }

        try {
            HashMap<String, Object> response = base.path("auth/signup")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(user), HashMap.class);

            System.out.println("Registration response: " + response);
            if (response != null) {
                message = response.get("message").toString();
                authType = "login"; // switch UI to login component immediately
                resetFields();
                return null;
            } // do NOT redirect so the current view updates and shows login part

        } catch (Exception e) {
            e.printStackTrace();
            message = "Registration failed: " + e.getMessage();
            return null;
        }
        return null;
    }
    
    public String logOut() {
        loggedUser = new SessionedUser();
        return "/index.xhtml?faces-redirect=true";
    }

    public String getEmailOrUsername() {
        return emailOrUsername;
    }

    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLoggedUser(SessionedUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    // for now
    public User getLoggedUser() {
        return loggedUser;
    }

}
