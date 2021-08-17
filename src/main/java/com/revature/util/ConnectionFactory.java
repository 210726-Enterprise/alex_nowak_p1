package com.revature.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to create connections to the database.
 */
public class ConnectionFactory {
    private static Properties dbConnectionProps = new Properties();
    private static InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/resources/dbconnection.properties");

    private static String url = "";
    private static String username = "";
    private static String password = "";

    private static Connection connection;

    public static Connection getConnection(){
        try {
            dbConnectionProps.load(inputStream);
            url = dbConnectionProps.getProperty("DB_URL");
            username = dbConnectionProps.getProperty("DB_USERNAME");
            password = dbConnectionProps.getProperty("DB_PASSWORD");
            connection = DriverManager.getConnection(url, username, password);
        } catch(IOException e){
            System.out.println("File dbconnection.properties could not be found.");
            System.out.println("Please make sure dconnection.properties is in a directory called resources.");
        } catch (SQLException e) {
            e.getMessage();
        }

        return connection;
    }
}


