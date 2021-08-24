package com.revature.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to create connections to the database.
 */
public class ConnectionFactory {
    private static Properties dbConnectionProps = new Properties();

    private static String url = "";
    private static String username = "";
    private static String password = "";

    private static Connection connection;

    public static Connection getConnection(Properties dbConnectionProps){
        try {
            Class.forName("org.postgresql.Driver");
            url = dbConnectionProps.getProperty("DB_URL");
            username = dbConnectionProps.getProperty("DB_USERNAME");
            password = dbConnectionProps.getProperty("DB_PASSWORD");
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Could not connect to database.");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not find database driver.");
        }

        return connection;
    }
}


