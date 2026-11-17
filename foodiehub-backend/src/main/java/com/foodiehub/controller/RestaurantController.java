package com.foodiehub.controller;
import com.foodiehub.DBConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/restaurants")
public class RestaurantController {

    // GET: View all restaurants
    @GetMapping
    public ResponseEntity<?> getRestaurants() {

        List<Map<String, Object>> restaurants = new ArrayList<>();

        String sql = "SELECT * FROM restaurants ORDER BY restaurant_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> restaurant = new LinkedHashMap<>();

                restaurant.put("restaurantId", rs.getInt("restaurant_id"));
                restaurant.put("restaurantName", rs.getString("restaurant_name"));
                restaurant.put("cuisine", rs.getString("cuisine"));
                restaurant.put("address", rs.getString("address"));
                restaurant.put("phone", rs.getString("phone"));
                restaurant.put("rating", rs.getBigDecimal("rating"));

                restaurants.add(restaurant);
            }

            return ResponseEntity.ok(restaurants);

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to fetch restaurants"));
        }
    }

    // POST: Add a restaurant
    @PostMapping
    public ResponseEntity<?> addRestaurant(@RequestBody Map<String, Object> data) {

        String sql = "INSERT INTO restaurants " +
                "(restaurant_name, cuisine, address, phone, rating) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, (String) data.get("restaurantName"));
            ps.setString(2, (String) data.get("cuisine"));
            ps.setString(3, (String) data.get("address"));
            ps.setString(4, (String) data.get("phone"));

            Object rating = data.get("rating");
            if (rating == null || rating.toString().isBlank()) {
                ps.setBigDecimal(5, new java.math.BigDecimal("0.0"));
            } else {
                ps.setBigDecimal(5, new java.math.BigDecimal(rating.toString()));
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(Map.of(
                                    "success", true,
                                    "restaurantId", keys.getInt(1),
                                    "message", "Restaurant added successfully"
                            ));
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true,
                            "message", "Restaurant added successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false,
                            "message", "Unable to add restaurant"));
        }
    }

    // PUT: Update a restaurant
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRestaurant(
            @PathVariable int id,
            @RequestBody Map<String, Object> data) {

        String sql = "UPDATE restaurants SET restaurant_name=?, " +
                "cuisine=?, address=?, phone=?, rating=? WHERE restaurant_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, (String) data.get("restaurantName"));
            ps.setString(2, (String) data.get("cuisine"));
            ps.setString(3, (String) data.get("address"));
            ps.setString(4, (String) data.get("phone"));

            Object rating = data.get("rating");
            if (rating == null || rating.toString().isBlank()) {
                ps.setBigDecimal(5, new java.math.BigDecimal("0.0"));
            } else {
                ps.setBigDecimal(5, new java.math.BigDecimal(rating.toString()));
            }

            ps.setInt(6, id);

            int updated = ps.executeUpdate();

            if (updated == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Restaurant not found"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Restaurant updated successfully"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false,
                            "message", "Unable to update restaurant"));
        }
    }

    // DELETE: Remove a restaurant
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRestaurant(@PathVariable int id) {

        String sql = "DELETE FROM restaurants WHERE restaurant_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int deleted = ps.executeUpdate();

            if (deleted == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Restaurant not found"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Restaurant deleted successfully"
            ));

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false,
                            "message", "Unable to delete restaurant. It may be linked to existing records.")
            );
        }
    }
}
