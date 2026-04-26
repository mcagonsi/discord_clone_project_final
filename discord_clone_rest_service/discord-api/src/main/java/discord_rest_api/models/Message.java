package discord_rest_api.models;

public class Message {
    private int id;
    private int authorId;
    private String content;
    private String createdAt;
    private MsgAttachment attachment;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public MsgAttachment getAttachment() {
        return attachment;
    }

    public void setAttachment(MsgAttachment attachment) {
        this.attachment = attachment;
    }
}