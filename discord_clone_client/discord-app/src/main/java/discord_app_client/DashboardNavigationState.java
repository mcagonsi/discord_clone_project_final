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

    // defines the sidebar state and manages the views
    private String sideBarPanel = "DMs";
    private int serverId;

    // defines and manages the state of the main content area
    private String mainContentPanel = "friendsPanel";
    private String mainContentTitle;
    private int directChatId;
    private int channelId;

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

    public void toggleDirectChat(int chatId) {
        this.directChatId = chatId;
        this.mainContentPanel = "chatPanel";
        System.out.println("Toggled Direct Chat with ID: " + chatId);
    }

    public void toggleSelectedServer(int serverId) {
        this.serverId = serverId;
        this.sideBarPanel = "server";
        this.mainContentPanel = "aboutServerPanel";
        System.out.println("Toggled Server with ID: " + serverId);
    }

    public void toggleChannel(int channelId) {
        this.channelId = channelId;
        this.mainContentPanel = "serverChatPanel";
        System.out.println("Toggled Channel with ID: " + channelId);
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

    public int getDirectChatId() {
        return directChatId;
    }

    public void setDirectChatId(int directChatId) {
        this.directChatId = directChatId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
}