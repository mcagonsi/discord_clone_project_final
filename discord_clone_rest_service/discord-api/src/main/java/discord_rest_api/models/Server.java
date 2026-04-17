package discord_rest_api.models;

import java.io.Serializable;

public class Server implements Serializable {
    private int id;
    private String name;
    private String description;
    private int ownerId;
    private String created_at;
    private boolean isPublic;
    private String invite_code;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String createdAt) {
        this.created_at = createdAt;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getInviteCode() {
        return invite_code;
    }

    public void setInviteCode(String inviteCode) {
        this.invite_code = inviteCode;
    }

    public String getInviteCodeSlug() {
        return invite_code != null ? this.invite_code.toString() : null;
    }

}
