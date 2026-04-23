package discord_app_client;

import java.io.Serializable;

import discord_app_client.models.User;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;


@Named("sessionedUser")
@SessionScoped
public class SessionedUser extends User  {
    
}
