package com.foodiehub.controller;
import com.foodiehub.DBConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private static class FoodOrderLine {
        int foodId;
        int quantity;
        BigDecimal price;

        FoodOrderLine(int foodId, int quantity, BigDecimal price) {
            this.foodId = foodId;
            this.quantity = quantity;
            this.price = price;
        }
    }

    @PostMapping("/place")
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestBody Map<String, Object> data) {

        String email = data.get("email") instanceof String
                ? ((String) data.get("email")).trim() : "";

        String address = data.get("deliveryAddress") instanceof String
                ? ((String) data.get("deliveryAddress")).trim() : "";

        Object rawItems = data.get("items");

        if (email.isEmpty() || address.isEmpty()
                || !(rawItems instanceof List<?>)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email, delivery address and items are required.")
            );
        }

        List<?> items = (List<?>) rawItems;

        if (items.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Your cart is empty.")
            );
        }

        if (items.size() > 50) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Too many different items in one order.")
            );
        }

        try (Connection connection = DBConnection.getConnection()) {

            // Find customer
            int customerId;

            String customerSql =
                    "SELECT customer_id FROM customers WHERE email = ?";

            try (PreparedStatement ps =
                         connection.prepareStatement(customerSql)) {

                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                Map.of("message", "Customer account not found.")
                        );
                    }

                    customerId = rs.getInt("customer_id");
                }
            }

            // Validate food items and get trusted database prices
            List<FoodOrderLine> orderLines = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            String foodSql =
                    "SELECT price FROM food_items "
                    + "WHERE food_id = ? AND availability = 1";

            try (PreparedStatement foodStatement =
                         connection.prepareStatement(foodSql)) {

                for (Object itemObject : items) {

                    if (!(itemObject instanceof Map<?, ?>)) {
                        return ResponseEntity.badRequest().body(
                                Map.of("message", "Invalid cart item.")
                        );
                    }

                    Map<?, ?> item = (Map<?, ?>) itemObject;

                    Object foodIdValue = item.get("foodId");
                    Object quantityValue = item.get("quantity");

                    if (!(foodIdValue instanceof Number)
                            || !(quantityValue instanceof Number)) {
                        return ResponseEntity.badRequest().body(
                                Map.of("message", "Food ID and quantity are required.")
                        );
                    }

                    int foodId = ((Number) foodIdValue).intValue();
                    int quantity = ((Number) quantityValue).intValue();

                    if (foodId <= 0 || quantity <= 0 || quantity > 99) {
                        return ResponseEntity.badRequest().body(
                                Map.of("message", "Invalid food ID or quantity.")
                        );
                    }

                    foodStatement.setInt(1, foodId);

                    try (ResultSet rs = foodStatement.executeQuery()) {

                        if (!rs.next()) {
                            return ResponseEntity.badRequest().body(
                                    Map.of("message",
                                            "Food item unavailable. Food ID: " + foodId)
                            );
                        }

                        BigDecimal price = rs.getBigDecimal("price");

                        orderLines.add(
                                new FoodOrderLine(foodId, quantity, price)
                        );

                        totalAmount = totalAmount.add(
                                price.multiply(BigDecimal.valueOf(quantity))
                        );
                    }
                }
            }

            // Save order and its items together
            connection.setAutoCommit(false);

            try {
                int orderId;

                String orderSql =
                        "INSERT INTO orders "
                        + "(customer_id, total_amount, order_status, delivery_address) "
                        + "VALUES (?, ?, 'Pending', ?)";

                try (PreparedStatement orderStatement =
                             connection.prepareStatement(
                                     orderSql, Statement.RETURN_GENERATED_KEYS)) {

                    orderStatement.setInt(1, customerId);
                    orderStatement.setBigDecimal(2, totalAmount);
                    orderStatement.setString(3, address);

                    orderStatement.executeUpdate();

                    try (ResultSet keys =
                                 orderStatement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException("Order ID was not generated.");
                        }

                        orderId = keys.getInt(1);
                    }
                }

                String itemSql =
                        "INSERT INTO order_items "
                        + "(order_id, food_id, quantity, item_price) "
                        + "VALUES (?, ?, ?, ?)";

                try (PreparedStatement itemStatement =
                             connection.prepareStatement(itemSql)) {

                    for (FoodOrderLine line : orderLines) {

                        itemStatement.setInt(1, orderId);
                        itemStatement.setInt(2, line.foodId);
                        itemStatement.setInt(3, line.quantity);
                        itemStatement.setBigDecimal(4, line.price);

                        itemStatement.addBatch();
                    }

                    itemStatement.executeBatch();
                }

                connection.commit();

                Map<String, Object> result = new HashMap<>();
                result.put("message", "Order placed successfully!");
                result.put("orderId", orderId);
                result.put("totalAmount", totalAmount);
                result.put("orderStatus", "Pending");

                return ResponseEntity.ok(result);

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Unable to place order. Please try again.")
            );
        }
    }
}
