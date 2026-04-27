package discord_app_client;

import java.io.Serializable;
import java.util.List;

import discord_app_client.models.Server;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named("serverSearchResults")
@SessionScoped
public class PublicServerSearchResults implements Serializable {
    private List<Server> servers;

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

}