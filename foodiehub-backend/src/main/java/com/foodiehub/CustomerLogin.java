package com.foodiehub;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

public class CustomerLogin {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("===== FOODIEHUB CUSTOMER LOGIN =====");

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String sql = "SELECT customer_id, full_name, password "
                   + "FROM customers WHERE email = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String storedHash = result.getString("password");

                    if (BCrypt.checkpw(password, storedHash)) {
                        System.out.println("\nLogin successful!");
                        System.out.println("Welcome, "
                                + result.getString("full_name") + "!");
                    } else {
                        System.out.println("\nIncorrect password!");
                    }

                } else {
                    System.out.println("\nCustomer account not found!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }

        scanner.close();
    }
}
