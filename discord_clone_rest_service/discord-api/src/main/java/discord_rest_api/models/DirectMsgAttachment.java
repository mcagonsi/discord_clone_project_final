package discord_rest_api.models;

public class DirectMsgAttachment extends MsgAttachment {

    private int directChatMessageId;   

    public int getDirectChatMessageId() {
        return directChatMessageId;
    }

    public void setDirectChatMessageId(int directChatMessageId) {
        this.directChatMessageId = directChatMessageId;
    }
}