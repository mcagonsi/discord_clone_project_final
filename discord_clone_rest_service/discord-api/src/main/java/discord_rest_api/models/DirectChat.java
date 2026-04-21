package discord_rest_api.models;

import java.sql.PreparedStatement;
import java.sql.*;


import discord_rest_api.utils.DatabaseConnection;

public class DirectChat extends Chat {

    public static int checkOrCreateConversationIdForUsers(User user1, User user2) {
        int conversation_id = -1;
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id FROM direct_chats WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)")) {
                stmt.setInt(1, user1.getId());
                stmt.setInt(2, user2.getId());
                stmt.setInt(3, user2.getId());
                stmt.setInt(4, user1.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        conversation_id = rs.getInt("id");
                    }
                }
            }
            // for later scaling and indexing on db
            // int a = Math.min(user1.getId(), user2.getId());
            // int b = Math.max(user1.getId(), user2.getId());
            // try (PreparedStatement stmt = conn.prepareStatement(
            //         "SELECT id FROM direct_chats WHERE LEAST(sender_id, receiver_id) = ? AND GREATEST(sender_id, receiver_id) = ?")) {
            //     stmt.setInt(1, a);
            //     stmt.setInt(2, b);
            //     try (ResultSet rs = stmt.executeQuery()) {
            //         if (rs.next()) {
            //             conversation_id = rs.getInt("id");
            //         }
            //     }
            // }

            // create conversation if needed (return generated key)
            if (conversation_id == -1) {
                try (PreparedStatement createStmt = conn.prepareStatement(
                        "INSERT INTO direct_chats (sender_id, receiver_id) VALUES (?, ?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    createStmt.setInt(1, user1.getId());
                    createStmt.setInt(2, user2.getId());
                    int rowsAffected = createStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        try (ResultSet keys = createStmt.getGeneratedKeys()) {
                            if (keys.next()) {
                                conversation_id = keys.getInt(1);
                            }
                        }
                    }
                }
            }
           
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred while fetching or creating conversation", e);

        }
        return conversation_id;
    }
}