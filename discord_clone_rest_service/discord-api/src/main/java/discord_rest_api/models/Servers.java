package discord_rest_api.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("servers")
public class Servers {

    @Path("{sendingUID}/{receivingUID}/{inviteCode}")
    @GET
    public String sendServerInvite(
        @PathParam("sendingUID") int sendingUID,
        @PathParam("receivingUID") int receivingUID,
        @PathParam("inviteCode") String inviteCode
    ) {
        try (
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/DiscordClone");
        ) {
            try (
                Connection conn = ds.getConnection();
            ) {
                try (
                    PreparedStatement validateUserStmt = conn.prepareStatement(
                        "SELECT id FROM servers WHERE invite_code=?;"
                    );
                ) {
                    validateUserStmt.setString(1, inviteCode);
                    ResultSet rs = validateUserStmt.executeQuery();
                    
                }
            }
        }
    }
}
