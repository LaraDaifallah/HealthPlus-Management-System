package application;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection connect(){
        try {
            String url = System.getenv().getOrDefault("HEALTHPLUS_DB_URL",
                    "jdbc:mysql://localhost:3306/healthplus?useSSL=false&serverTimezone=UTC");
            String user = System.getenv().getOrDefault("HEALTHPLUS_DB_USER", "root");
            String password = System.getenv("HEALTHPLUS_DB_PASSWORD");
            if (password == null) {
                throw new IllegalStateException("Set HEALTHPLUS_DB_PASSWORD before starting HealthPlus.");
            }
            Connection conn =DriverManager.getConnection(url, user, password);
            System.out.println("database connected!");
            return conn;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}