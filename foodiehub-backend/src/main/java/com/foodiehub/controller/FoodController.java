package com.foodiehub.controller;
import com.foodiehub.DBConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/foods")
public class FoodController {

    // GET: View all food items
    @GetMapping
    public ResponseEntity<?> getFoods() {

        List<Map<String, Object>> foods = new ArrayList<>();

        String sql = """
            SELECT f.food_id, f.restaurant_id, f.food_name,
                   f.description, f.price, f.category,
                   f.image_url, f.availability, r.restaurant_name
            FROM food_items f
            LEFT JOIN restaurants r
                ON f.restaurant_id = r.restaurant_id
            ORDER BY f.food_id DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Map<String, Object> food = new LinkedHashMap<>();

                food.put("foodId", rs.getInt("food_id"));

                int restaurantId = rs.getInt("restaurant_id");
                food.put("restaurantId",
                        rs.wasNull() ? null : restaurantId);

                food.put("foodName", rs.getString("food_name"));
                food.put("description", rs.getString("description"));
                food.put("price", rs.getBigDecimal("price"));
                food.put("category", rs.getString("category"));
                food.put("imageUrl", rs.getString("image_url"));
                food.put("availability", rs.getBoolean("availability"));
                food.put("restaurantName", rs.getString("restaurant_name"));

                foods.add(food);
            }

            return ResponseEntity.ok(foods);

        } catch (SQLException e) {

            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to fetch food items"));
        }
    }

    // POST: Add a food item
    @PostMapping
    public ResponseEntity<?> addFood(@RequestBody Map<String, Object> data) {

        String sql = """
            INSERT INTO food_items
            (restaurant_id, food_name, description, price,
             category, image_url, availability)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            setFoodParameters(ps, data, false);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (keys.next()) {

                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(Map.of(
                                    "success", true,
                                    "foodId", keys.getInt(1),
                                    "message", "Food item added successfully"
                            ));
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Food item added successfully"
                    ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (SQLException e) {

            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false,
                            "message", "Unable to add food item"));
        }
    }

    // PUT: Update a food item
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFood(
            @PathVariable int id,
            @RequestBody Map<String, Object> data) {

        String sql = """
            UPDATE food_items
            SET restaurant_id = ?, food_name = ?, description = ?,
                price = ?, category = ?, image_url = ?, availability = ?
            WHERE food_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setFoodParameters(ps, data, false);
            ps.setInt(8, id);

            int updated = ps.executeUpdate();

            if (updated == 0) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Food item not found"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Food item updated successfully"
            ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (SQLException e) {

            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false,
                            "message", "Unable to update food item"));
        }
    }

    // DELETE: Remove a food item
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFood(@PathVariable int id) {

        String sql = "DELETE FROM food_items WHERE food_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int deleted = ps.executeUpdate();

            if (deleted == 0) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Food item not found"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Food item deleted successfully"
            ));

        } catch (SQLException e) {

            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false,
                            "message", "Unable to delete food item. It may be linked to existing orders."));
        }
    }

    // Validate and set food details
    private void setFoodParameters(
            PreparedStatement ps,
            Map<String, Object> data,
            boolean unused) throws SQLException {

        String foodName = stringValue(data.get("foodName"));

        if (foodName.isBlank()) {
            throw new IllegalArgumentException("Food name is required");
        }

        Object priceValue = data.get("price");

        if (priceValue == null || priceValue.toString().isBlank()) {
            throw new IllegalArgumentException("Price is required");
        }

        BigDecimal price;

        try {
            price = new BigDecimal(priceValue.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid price");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        Object restaurantValue = data.get("restaurantId");

        if (restaurantValue == null ||
                restaurantValue.toString().isBlank()) {
            ps.setNull(1, Types.INTEGER);
        } else {
            try {
                ps.setInt(1, Integer.parseInt(restaurantValue.toString()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid restaurant ID");
            }
        }

        ps.setString(2, foodName);
        ps.setString(3, stringValue(data.get("description")));
        ps.setBigDecimal(4, price);
        ps.setString(5, stringValue(data.get("category")));
        ps.setString(6, stringValue(data.get("imageUrl")));

        Object availabilityValue = data.get("availability");

        boolean available = availabilityValue == null ||
                Boolean.parseBoolean(availabilityValue.toString());

        ps.setBoolean(7, available);
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
