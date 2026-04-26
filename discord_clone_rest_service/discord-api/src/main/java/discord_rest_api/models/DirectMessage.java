package discord_rest_api.models;

public class DirectMessage extends Message {
    private int directChatId;

    // Getters and Setters
    public int getDirectChatId() {
        return directChatId;
    }

    public void setDirectChatId(int directChatId) {
        this.directChatId = directChatId;
    }
}
