package discord_rest_api.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import discord_rest_api.utils.DatabaseConnection;


public class Friends {

    private User getUserIdFromUserUID(String user_uid) {
        User user = null;
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE user_uid=?;"
            )
        ) {
            stmt.setString(1, user_uid);
            try (
                ResultSet rs = stmt.executeQuery();
            ) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordBytes(rs.getBytes("password"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public void getFriendsList(String user_uid) {
        try {
            User user = getUserIdFromUserUID(user_uid);
            try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT friend_user_id FROM friends WHERE user_id=? AND accepted=1;"
                )
            ) {
                stmt.setInt(1, user.getId());
                try (
                    ResultSet rs = stmt.executeQuery();
                ) {
                   //TODO: Finish writing this
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
    
}
