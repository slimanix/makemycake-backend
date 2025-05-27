package com.bootcamp.makemycake;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        // Database credentials
        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String database = System.getenv("DB_NAME");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        // Construct the connection URL
        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true", 
            host, port, database);

        System.out.println("Attempting to connect to database...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);

        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Attempt to connect
            Connection connection = DriverManager.getConnection(url, username, password);
            
            System.out.println("Connection successful!");
            System.out.println("Database: " + connection.getCatalog());
            System.out.println("Server: " + connection.getMetaData().getDatabaseProductName() + 
                             " " + connection.getMetaData().getDatabaseProductVersion());
            
            // Close the connection
            connection.close();
            
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 