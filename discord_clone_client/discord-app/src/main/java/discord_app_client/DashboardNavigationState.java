package discord_app_client;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named("dashboardNavigation")
@SessionScoped
public class DashboardNavigationState implements Serializable {

    private boolean showProfile = false;
    private boolean showNotifications = false;

    // defines the server Rails state
    private String selectedServer;

    // defines the sidebar state and manages the views
    private String sideBarPanel = "DMs";
    private String selectedChannel;
    private String selectedDirectChat;

    // defines and manages the state of the main content area
    private String mainContentPanel = "chatPanel";
    private String mainContentTitle;

    // defines and manage main content area side info on right

    public void toggleShowProfile() {
        this.showProfile = !this.showProfile;
        System.out.println("Show Profile: " + this.showProfile);
    }

    public void toggleShowNotifications() {
        this.showNotifications = !this.showNotifications;
        System.out.println("Show Notifications: " + this.showNotifications);
    }

    public void toggleSideBarPanel(String panel) {
        this.sideBarPanel = panel;
        System.out.println("Side Bar Panel: " + this.sideBarPanel);
    }

    public void toggleMainContentPanel(String panel) {
        this.mainContentPanel = panel;
        System.out.println("Main Content Panel: " + this.mainContentPanel);
    }

    // just for testing
    public List<String> getServers() {
        return Arrays.asList("Server 1", "Server 2", "Server 3");
    }

    public List<String> getDirectMessages() {
        return Arrays.asList("DM 1", "DM 2", "DM 3");
    }

    public void selectServer(String server) {
        this.selectedServer = server;
        System.out.println("Selected Server: " + this.selectedServer);
    }

    public boolean isShowProfile() {
        return showProfile;
    }

    public void setShowProfile(boolean showProfile) {
        this.showProfile = showProfile;
    }

    public boolean isShowNotifications() {
        return showNotifications;
    }

    public void setShowNotifications(boolean showNotifications) {
        this.showNotifications = showNotifications;
    }

    public void setSideBarPanel(String sideBarPanel) {
        this.sideBarPanel = sideBarPanel;

    }

    public String getSideBarPanel() {
        return sideBarPanel;
    }

    public String getMainContentPanel() {
        return mainContentPanel;
    }

    public void setMainContentPanel(String mainContentPanel) {
        this.mainContentPanel = mainContentPanel;
    }

}