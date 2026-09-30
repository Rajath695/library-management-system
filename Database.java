import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private static String URL, USER, PASS;

    static {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties p = new Properties();
            p.load(fis);
            URL = p.getProperty("db.url");
            USER = p.getProperty("db.user");
            PASS = p.getProperty("db.pass");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}