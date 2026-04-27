package discord_app_client.models;

public class DirectChatMessage extends Message {
    private int directChatId;

    public int getDirectChatId() {
        return directChatId;
    }

    public void setDirectChatId(int directChatId) {
        this.directChatId = directChatId;
    }
    
}
