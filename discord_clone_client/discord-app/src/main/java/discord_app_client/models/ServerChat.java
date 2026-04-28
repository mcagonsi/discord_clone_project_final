package discord_app_client.models;

import java.util.ArrayList;
import java.util.List;

// ServerChats are Same as Channels but for sake of clarity we used Server Chat
public class ServerChat extends Chat {
    private int serverId;
    List<Channel> channels = new ArrayList<>();


    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }
    public void clear() {
        this.channels.clear();
    }
}
