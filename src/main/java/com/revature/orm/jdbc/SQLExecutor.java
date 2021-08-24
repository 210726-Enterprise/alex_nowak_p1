package com.revature.orm.jdbc;

import com.revature.util.ConnectionFactory;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to execute SQL statements.
 */
public class SQLExecutor {
    private final static Logger logger = Logger.getLogger(SQLExecutor.class);

    /**
     * Executes a SQL statement and returns the number the rows that were changed.
     * This method is meant for Data Manipulation Language, usually statements like
     * INSERT, UPDATE, and DELETE.
     * @param sql a String of the SQL statement to be executed
     * @return the number of rows that were changed by the update
     */
    public static int doUpdate(String sql, Properties connectionProps){
        int updatedRows = 0;
        try(Connection connection = ConnectionFactory.getConnection(connectionProps)){
            PreparedStatement statement = connection.prepareStatement(sql);
            updatedRows = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return updatedRows;
    }

    /**
     * Executes a SQL query and returns a ResultSet of the rows that were retrieved.
     * @param sql a String of the SQL statement to be executed
     * @return a ResultSet of all the rows retrieved
     */
    public static ResultSet doQuery(String sql, Properties connectionProps){
        ResultSet resultSet = null;
        try(Connection connection = ConnectionFactory.getConnection(connectionProps)){
            PreparedStatement statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
           logger.error(e.getMessage(), e);
        }
        return resultSet;
    }
}
