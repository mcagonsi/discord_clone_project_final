package discord_app_client.models;

import java.io.Serializable;

import discord_app_client.utils.Variables;

public class Server implements Serializable {
    private int id;
    private String name;
    private String description;
    private int ownerId;
    private String created_at;
    private boolean publicStatus;
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
        return publicStatus;
    }

    public void setPublicStatus(boolean publicStatus) {
        this.publicStatus = publicStatus;
    }

    public String getInviteCode() {
        return invite_code;
    }

    public void setInviteCode(String inviteCode) {
        this.invite_code = inviteCode;
    }

    public String getInviteCodeSlug() {
        return invite_code != null ? Variables.DOMAIN_URL + "/" + this.id + "/" + this.invite_code.toString()
                : null;
    }

}
