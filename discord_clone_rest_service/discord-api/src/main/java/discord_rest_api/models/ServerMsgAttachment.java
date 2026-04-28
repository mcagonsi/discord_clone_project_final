package discord_rest_api.models;

public class ServerMsgAttachment extends MsgAttachment {

    private int messageId;
    
    public int getDirectChatMessageId() {
        return messageId;
    }

    public void setDirectChatMessageId(int directChatMessageId) {
        this.messageId = directChatMessageId;
    }

}