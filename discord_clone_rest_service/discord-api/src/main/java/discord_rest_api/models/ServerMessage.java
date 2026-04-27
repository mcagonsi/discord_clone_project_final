package discord_rest_api.models;

public class ServerMessage extends Message {
    private int channelId;

    // Getters and Setters
    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
}
