package discord_rest_api.models;

public class DirectMsgAttachment extends MsgAttachment {

    private int directChatMessageId;
    private String filename;
    

    public int getDirectChatMessageId() {
        return directChatMessageId;
    }

    public void setDirectChatMessageId(int directChatMessageId) {
        this.directChatMessageId = directChatMessageId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}