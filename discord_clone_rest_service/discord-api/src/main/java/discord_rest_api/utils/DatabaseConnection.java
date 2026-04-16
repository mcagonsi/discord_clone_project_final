package discord_rest_api.utils;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.naming.Context;
import java.sql.Connection;

public class DatabaseConnection {
    public static Connection getConnection(){
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/DiscordClone");
            return ds.getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
        
    }
}
