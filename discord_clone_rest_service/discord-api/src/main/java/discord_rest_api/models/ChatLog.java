package discord_rest_api.models;

import java.io.Serializable;
import java.util.List;

public class ChatLog implements Serializable {
    
    private List<Message> messages;
    

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

}