package com.foodiehub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class App {

    private static final String URL =
            "jdbc:mysql://localhost:3306/foodiehub";

    private static final String USER = "root";

    // Replace this with your own MySQL root password
    private static final String PASSWORD = "Rehaan@786";

    public static void main(String[] args) {

        try {
            Connection connection = DriverManager.getConnection(
                    URL, USER, PASSWORD
            );

            System.out.println("================================");
            System.out.println(" FOODIEHUB DATABASE CONNECTED!");
            System.out.println("================================");

            connection.close();

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}
