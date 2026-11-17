package com.foodiehub.controller;
import com.foodiehub.DBConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class MyOrdersController {

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@RequestParam String email) {

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is required."));
        }

        String sql =
                "SELECT o.order_id, o.total_amount, o.order_status, " +
                "o.delivery_address, o.order_date, " +
                "oi.food_id, oi.quantity, oi.item_price, " +
                "f.food_name " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "LEFT JOIN order_items oi ON o.order_id = oi.order_id " +
                "LEFT JOIN food_items f ON oi.food_id = f.food_id " +
                "WHERE c.email = ? " +
                "ORDER BY o.order_date DESC, o.order_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim());

            try (ResultSet rs = statement.executeQuery()) {

                Map<Integer, Map<String, Object>> orders =
                        new LinkedHashMap<>();

                while (rs.next()) {

                    int orderId = rs.getInt("order_id");

                    if (!orders.containsKey(orderId)) {

                        Map<String, Object> order = new LinkedHashMap<>();

                        order.put("orderId", orderId);
                        order.put("totalAmount", rs.getBigDecimal("total_amount"));
                        order.put("orderStatus", rs.getString("order_status"));
                        order.put("deliveryAddress", rs.getString("delivery_address"));
                        order.put("orderDate", rs.getTimestamp("order_date"));
                        order.put("items", new ArrayList<Map<String, Object>>());

                        orders.put(orderId, order);
                    }

                    int foodId = rs.getInt("food_id");

                    if (!rs.wasNull()) {

                        Map<String, Object> item = new LinkedHashMap<>();

                        item.put("foodId", foodId);
                        item.put("foodName", rs.getString("food_name"));
                        item.put("quantity", rs.getInt("quantity"));
                        item.put("itemPrice", rs.getBigDecimal("item_price"));

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> items =
                                (List<Map<String, Object>>) orders
                                        .get(orderId).get("items");

                        items.add(item);
                    }
                }

                return ResponseEntity.ok(
                        new ArrayList<>(orders.values())
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to retrieve orders."));
        }
    }
}
