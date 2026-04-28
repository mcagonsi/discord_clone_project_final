package discord_app_client.models;

import java.io.Serializable;

public class Channel implements Serializable {
    private int id;
    private String name;
    private String createdBy;
    private int serverId;

    public int getChannelId() {
        return id;
    }

    public String getChannelName() {
        return name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setChannelId(int channelId) {
        this.id = channelId;
    }

    public void setChannelName(String channelName) {
        this.name = channelName;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getServerId() {
        return serverId;
    }
    public void setServerId(int serverId) {
        this.serverId = serverId;
    }
}
