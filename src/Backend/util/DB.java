package Backend.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DB {
    private static HikariConfig AuthConfiguration = new HikariConfig();
    private static HikariDataSource AuthenticationDataSource;
    private static HikariDataSource ERPDataSource;
    private static HikariConfig ERPConfiguration = new HikariConfig();

    private final static String Username = "root";
    private final static String Password = "MyNewPass";

    static {
        AuthConfiguration.setJdbcUrl("jdbc:mysql://localhost:3306/auth_db");
        AuthConfiguration.setUsername(Username);
        AuthConfiguration.setPassword(Password);
        AuthenticationDataSource = new HikariDataSource(AuthConfiguration);
    }

    static {
        ERPConfiguration.setJdbcUrl("jdbc:mysql://localhost:3306/erp_db");
        ERPConfiguration.setUsername(Username);
        ERPConfiguration.setPassword(Password);
        ERPDataSource = new HikariDataSource(ERPConfiguration);
    }

    public static DataSource GetAuthenticanDataSource() {
        return AuthenticationDataSource;
    }

    public static DataSource GetERPDataSource() {
        return ERPDataSource;
    }

    public static String GetUsername() {
        return Username;
    }

    public static String GetPassword() {
        return Password;
    }
}
