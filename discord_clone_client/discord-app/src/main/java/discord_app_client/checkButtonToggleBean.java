package discord_app_client;


import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;

@Named("checkButtonToggleBean")
@SessionScoped
public class checkButtonToggleBean implements Serializable {
    
    private String activePanel = "create";

    public String getActivePanel() {
        return activePanel;
    }

    public void showCreate() {
        activePanel = "create";
    }

    public void showDMs() {
        activePanel = "DMs";
    }

    public void showServer() {
        activePanel = "currentServer";
    }

    public void showServers() {
        activePanel = "servers";
    }
}
