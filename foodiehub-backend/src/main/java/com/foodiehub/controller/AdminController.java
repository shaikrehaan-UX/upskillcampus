package com.foodiehub.controller;

import com.foodiehub.DBConnection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final HttpSessionSecurityContextRepository securityRepository =
            new HttpSessionSecurityContextRepository();

    // ADMIN LOGIN
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> adminLogin(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Map<String, Object> response = new LinkedHashMap<>();

        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null ||
                username.isBlank() || password.isBlank()) {

            response.put("success", false);
            response.put("message", "Username and password are required");

            return ResponseEntity.badRequest().body(response);
        }

        String sql = "SELECT password_hash FROM admins WHERE username = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next() ||
                        !BCrypt.checkpw(password, rs.getString("password_hash"))) {

                    response.put("success", false);
                    response.put("message", "Invalid username or password");

                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(response);
                }
            }

            // Create authenticated admin session
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(
                                    new SimpleGrantedAuthority("ROLE_ADMIN")
                            )
                    );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Save authentication in the HTTP session
            securityRepository.saveContext(
                    context, httpRequest, httpResponse
            );

            response.put("success", true);
            response.put("message", "Admin login successful");
            response.put("username", username);

            return ResponseEntity.ok(response);

        } catch (SQLException e) {

            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Database error");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ADMIN LOGOUT
    @PostMapping("/logout")
    public Map<String, Object> adminLogout(
            HttpServletRequest request,
            HttpServletResponse response) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        new SecurityContextLogoutHandler().logout(
                request, response, authentication
        );

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("success", true);
        result.put("message", "Admin logged out successfully");

        return result;
    }

    // VIEW ALL CUSTOMER ORDERS
    @GetMapping("/orders")
    public List<Map<String, Object>> getAllOrders() {

        List<Map<String, Object>> orders = new ArrayList<>();

        String sql = """
            SELECT o.order_id, c.full_name, c.email,
                   o.total_amount, o.order_status,
                   o.delivery_address, o.order_date
            FROM orders o
            JOIN customers c ON o.customer_id = c.customer_id
            ORDER BY o.order_id DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Map<String, Object> order = new LinkedHashMap<>();

                int orderId = rs.getInt("order_id");

                order.put("orderId", orderId);
                order.put("customerName", rs.getString("full_name"));
                order.put("email", rs.getString("email"));
                order.put("totalAmount", rs.getBigDecimal("total_amount"));
                order.put("orderStatus", rs.getString("order_status"));
                order.put("deliveryAddress", rs.getString("delivery_address"));
                order.put("orderDate", rs.getTimestamp("order_date"));

                List<Map<String, Object>> items = new ArrayList<>();

                String itemSql = """
                    SELECT f.food_name, oi.quantity, oi.item_price
                    FROM order_items oi
                    JOIN food_items f ON oi.food_id = f.food_id
                    WHERE oi.order_id = ?
                    """;

                try (PreparedStatement itemPs =
                             con.prepareStatement(itemSql)) {

                    itemPs.setInt(1, orderId);

                    try (ResultSet itemRs = itemPs.executeQuery()) {

                        while (itemRs.next()) {

                            Map<String, Object> item = new LinkedHashMap<>();

                            item.put("foodName", itemRs.getString("food_name"));
                            item.put("quantity", itemRs.getInt("quantity"));
                            item.put("itemPrice",
                                    itemRs.getBigDecimal("item_price"));

                            items.add(item);
                        }
                    }
                }

                order.put("items", items);
                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    // UPDATE ORDER STATUS
    @PutMapping("/orders/{orderId}/status")
    public Map<String, Object> updateOrderStatus(
            @PathVariable int orderId,
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        String status = request.get("status");

        List<String> allowedStatuses = Arrays.asList(
                "Pending",
                "Preparing",
                "Out for Delivery",
                "Delivered",
                "Cancelled"
        );

        if (status == null || !allowedStatuses.contains(status)) {

            response.put("success", false);
            response.put("message", "Invalid order status");

            return response;
        }

        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);

            int updated = ps.executeUpdate();

            if (updated > 0) {

                response.put("success", true);
                response.put("message", "Order status updated successfully");
                response.put("orderId", orderId);
                response.put("status", status);

            } else {

                response.put("success", false);
                response.put("message", "Order not found");
            }

        } catch (SQLException e) {

            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Database error");
        }

        return response;
    }
}
