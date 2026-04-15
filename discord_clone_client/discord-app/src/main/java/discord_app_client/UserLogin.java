package discord_app_client;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.naming.Context;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Named("userLoginBean")
@SessionScoped
public class UserLogin implements Serializable{
    @NotNull
    private String name;
    @NotNull
    @NotBlank
    @Size(min=12,max=36)
    private String userPassword;
    private String token;
    private String message;
    private int userId;
    private Connection conn;

    private boolean verifyPassword(byte[] bytes) throws UnsupportedEncodingException{
        return BCrypt.verifyer().verify(userPassword.getBytes("UTF-16"), bytes).verified;
    }


    @PostConstruct
    public void openConnection(){
        try {
            this.token = null;
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/GroupsAndFriends");
            this.conn = ds.getConnection();

        } catch (NamingException | SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @PreDestroy
    public void closeConnection(){
        if (this.conn != null) {
            try {
                this.conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void updateToken() {
        try (
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT userID, token, PW_Hash FROM users WHERE name=?;"
            )
        ) {
            stmt.setString(1, name);
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                if (rs.next()) {
                    if (verifyPassword(rs.getBytes(3))) {
                        this.token = rs.getString(2);
                        this.userId = rs.getInt(1);
                        this.message = "";
                    } else {
                        this.message = "Invalid login";
                    }
                } else {
                    this.message = "Invalid login";
                }
            } catch (SQLException | UnsupportedEncodingException e) {
                    this.message = e.getMessage();
                    return;
            }
        } catch (SQLException e) {
            this.message = e.getMessage();
            return;
        }
    }

    public void signup() {
        try (
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (name, PW_Hash, token) VALUES (?, ?, SHA2(RAND(), 256));");
        ) {
            byte[] hash = BCrypt.withDefaults().hash(12, userPassword.getBytes("UTF-16"));

            stmt.setString(1, name);
            stmt.setBytes(2, hash);
            
            int rows = stmt.executeUpdate();
            if (rows != 1) {
                this.message = "Failed to create new user: " + name;
            } else {
                this.message = "Successfully created new user: " + name;
            }
        } catch (SQLException | UnsupportedEncodingException e) {
            this.message = e.getMessage();
            return;
        }
    }

    public String getName() {
        return name;
    }
    public void setName(String userName) {
        this.name = userName;
    }
    public String getUserPassword() {
        return userPassword;
    }
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    public String getToken() {
        return token;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getMessage() {
        return message;
    }
}
