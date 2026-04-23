package discord_app_client;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named("dashboard")
@SessionScoped
public class Dashboard implements Serializable {
    private String title;
    private String MainMenuOption;
    private String MenuSubOption;
    private String serverRailOption = "DMs";
    private boolean showProfile = false;

    public void toggleShowProfile() {
        this.showProfile = !this.showProfile;
        System.out.println("Show Profile: " + this.showProfile);
    }

    public void toggleServerRailOption(String option) {
        this.serverRailOption = option;
        System.out.println("Server Rail Option: " + this.serverRailOption);
    }

    //just for testing
    public List<String> getServers() {
        return Arrays.asList("Server 1", "Server 2", "Server 3");
    }
  
    public List<String> getDirectMessages() {
        return Arrays.asList("DM 1", "DM 2", "DM 3");
    }
  

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setMainMenuOption(String MainMenuOption) {
        this.MainMenuOption = MainMenuOption;
    }

    public String getMainMenuOption() {
        return MainMenuOption;
    }

    public void setMenuSubOption(String MenuSubOption) {
        this.MenuSubOption = MenuSubOption;
    }

    public String getMenuSubOption() {
        return MenuSubOption;
    }

    public boolean isShowProfile() {
        return showProfile;
    }

    public void setShowProfile(boolean showProfile) {
        this.showProfile = showProfile;
    }

    public void setServerRailOption(String serverRailOption) {
        this.serverRailOption = serverRailOption;
        
    }

    public String getServerRailOption() {
        return serverRailOption;
    }

}