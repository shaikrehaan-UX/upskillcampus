package com.foodiehub;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

public class CustomerRegistration {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("===== FOODIEHUB CUSTOMER REGISTRATION =====");

        System.out.print("Enter full name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine();

        System.out.print("Enter address: ");
        String address = scanner.nextLine();

        // Hash the password using BCrypt
        String hashedPassword = BCrypt.hashpw(
            password,
            BCrypt.gensalt(12)
        );

        String sql = "INSERT INTO customers "
                + "(full_name, email, password, phone, address) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, hashedPassword);
            statement.setString(4, phone);
            statement.setString(5, address);

            int result = statement.executeUpdate();

            if (result > 0) {
                System.out.println("Registration successful!");
                System.out.println("Password securely hashed!");
            }

        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }

        scanner.close();
    }
}
