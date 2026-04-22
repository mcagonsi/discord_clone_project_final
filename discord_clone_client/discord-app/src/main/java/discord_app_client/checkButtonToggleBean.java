package discord_app_client;

import java.io.Serializable;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named("checkButtonToggleBean")
@ViewScoped
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
