package discord_rest_api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord_rest_api.models.Server;
import discord_rest_api.models.User;
import discord_rest_api.utils.CommonGetters;
import discord_rest_api.utils.DatabaseConnection;
import discord_rest_api.utils.PermissionConst;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("channel")
public class Channel {

    @POST
    @Path("create")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> createChannel(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        User user = CommonGetters.getUserFromUserUID(JSON.get("user_uid"));
        Server server = CommonGetters.getServerById(Integer.parseInt(JSON.get("server_id")));
        String channelName = JSON.get("channel_name");
        
        if (CheckPermission.checkPermissionByName(user, server, PermissionConst.CREATE_CHANNEL) && server != null) {
            try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO channels (server_id, name, created_by) VALUES (?, ?, ?);"
                );
            ) {
                stmt.setInt(1, server.getId());
                stmt.setString(2, channelName);
                stmt.setInt(3, user.getId());

                int result = stmt.executeUpdate();

                if (result == 1) {
                    response.put("message", "Channel '" + channelName + "' created successfully");
                } else {
                    response.put("message", "Failed to create channel");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.put("message", "Failed to create channel");
            }
        } else {
            if (server != null) {
                response.put("message", user.getDisplay_name() + " does not have permission to create channels in " + server.getName());
            } else {
                response.put("message", "Could not find server");
            }
        }
        return response;
    }

    @POST
    @Path("list")
    @Produces("application/json")
    @Consumes("application/json")
    public HashMap<String, Object> getChannelList(HashMap<String, String> JSON) {
        HashMap<String, Object> response = new HashMap<>();
        List<discord_rest_api.models.Channel> channels = new ArrayList<discord_rest_api.models.Channel>();
        Server server = CommonGetters.getServerById(Integer.parseInt(JSON.get("server_id")));
        if (server != null) {
            try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM channels WHERE server_id=?"
                );
            ) {
                stmt.setInt(1, server.getId());

                try (
                    ResultSet rs = stmt.executeQuery();
                ) {
                    while (rs.next()) {
                        discord_rest_api.models.Channel channel = new discord_rest_api.models.Channel();
                        channel.setId(rs.getInt("id"));
                        channel.setName(rs.getString("name"));
                        channel.setCreatedBy(rs.getInt("created_by"));
                        channel.setServerId(rs.getInt("server_id"));
                        channels.add(channel);
                    }
                    response.put("channels", channels);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            response.put("message", "Could not find server");
        }
        return response;
    }
}
