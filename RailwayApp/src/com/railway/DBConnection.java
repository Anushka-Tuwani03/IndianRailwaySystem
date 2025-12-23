package com.railway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/IndianRailways";
    private static final String USER = "root";
    private static final String PASSWORD = "Laddu18";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if(conn != null){
                System.out.println("Database Connected Successfully!");
            } else {
                System.out.println("Connection Failed!");
            }
        } catch(SQLException e){
            e.printStackTrace();
            System.out.println("Error connecting to database!");
        }
    }
}
