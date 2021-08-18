package com.revature.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
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

    private static String url = "";
    private static String username = "";
    private static String password = "";

    private static Connection connection;

    public static Connection getConnection(){
        try {
            Class.forName("org.h2.Driver");
            dbConnectionProps.load(new FileReader("src/main/resources/dbconnection.properties"));
            url = dbConnectionProps.getProperty("DB_URL");
            username = dbConnectionProps.getProperty("DB_USERNAME");
            password = dbConnectionProps.getProperty("DB_PASSWORD");
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Could not connect to database.");
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File dbconnection.properties could not be found.");
            System.out.println("Please make sure dconnection.properties is in a directory called resources.");
        } catch (IOException ioException) {
            System.out.println("Error reading from database configuration.");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not find database driver.");
        }

        return connection;
    }
}


