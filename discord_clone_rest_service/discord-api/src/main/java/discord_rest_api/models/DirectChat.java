package discord_rest_api.models;

import java.sql.PreparedStatement;
import java.sql.*;


import discord_rest_api.utils.DatabaseConnection;

public class DirectChat extends Chat {

    private int id;
    private int senderId;
    private int receiverId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }
    
}