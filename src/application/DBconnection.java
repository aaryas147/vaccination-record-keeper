package application;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBconnection {
    private static final String URL = "jdbc:mysql://localhost:3306/vaccination_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; // your MySQL username
    private static final String PASSWORD = "aarya#040107"; // ⚠️ change this

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // ensure driver loads
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to MySQL successfully!");
            return conn;
        } catch (Exception e) {
            e.printStackTrace(); // will print the exact cause
            return null;
        }
    }
}
