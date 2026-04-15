package discord_app_client;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;


@Named("permissionBean")
@SessionScoped
public class App implements Serializable{

   private Client client;
   private WebTarget base;
   private String permissions;

   @PostConstruct
   public void init() {
    client = ClientBuilder.newClient();
    base = client.target("http://localhost:8080/discord-api");
   }

   @PreDestroy
   public void cleanup() {
    client.close();
   }

   public void getPermissions() {
    // Implementation for fetching permissions
    permissions = base.path("permissions").request(MediaType.APPLICATION_JSON).get(String.class);
    System.out.println(permissions);
   }
}
