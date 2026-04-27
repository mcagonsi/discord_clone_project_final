package discord_app_client;

import discord_app_client.models.Server;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named("privateServerToJoin")
@SessionScoped
public class PrivateServerToJoin extends Server {
    
}
