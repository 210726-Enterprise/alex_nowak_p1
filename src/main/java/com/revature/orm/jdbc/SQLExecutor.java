package com.revature.orm.jdbc;

import com.revature.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLExecutor {
    public static int doUpdate(String sql){
        int updatedRows = 0;
        try(Connection connection = ConnectionFactory.getConnection()){
            PreparedStatement statement = connection.prepareStatement(sql);
            updatedRows = statement.executeUpdate();
        } catch (SQLException e) {
            //TODO change to logging
            System.out.println(e.getMessage());
        }
        return updatedRows;
    }

    public static void doQuery(String sql){
        ResultSet resultSet = null;
        try(Connection connection = ConnectionFactory.getConnection()){
            PreparedStatement statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            //TODO change to logging
            System.out.println(e.getMessage());
        }
    }
}
