package com.foodiehub.controller;
import com.foodiehub.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> data) {

        String sql = "INSERT INTO customers "
                + "(full_name, email, password, phone, address) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, data.get("fullName"));
            statement.setString(2, data.get("email"));
            statement.setString(3,
                    BCrypt.hashpw(data.get("password"), BCrypt.gensalt(12)));
            statement.setString(4, data.get("phone"));
            statement.setString(5, data.get("address"));

            statement.executeUpdate();

            return "Customer registered successfully!";

        } catch (SQLException e) {
            return "Registration failed: " + e.getMessage();
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> data) {

        String sql = "SELECT full_name, password "
                + "FROM customers WHERE email = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, data.get("email"));

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String storedHash = result.getString("password");

                    if (storedHash.startsWith("$2")
                            && BCrypt.checkpw(data.get("password"), storedHash)) {
                        return "Welcome, " + result.getString("full_name") + "!";
                    }

                    return "Incorrect password!";
                }

                return "Customer account not found!";
            }

        } catch (SQLException e) {
            return "Login failed: " + e.getMessage();
        }
    }
}
