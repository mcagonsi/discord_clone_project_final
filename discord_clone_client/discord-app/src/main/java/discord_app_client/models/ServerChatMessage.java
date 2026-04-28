package discord_app_client.models;

public class ServerChatMessage {
    private int id;
    private int channelId;
    private String content;
    private int senderId;
    private String createdAt;
    private boolean deleted;

    public int getId() {
        return id;
    }

    public int getChannelId() {
        return channelId;

    }

    public String getContent() {
        return content;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}