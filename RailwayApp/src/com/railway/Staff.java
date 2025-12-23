package com.railway;

import java.sql.*;

public class Staff {
    int staffId;
    int userId;
    String name;
    String role;
    String phone;
    
    public void addStaff() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO staff (user_id, name, role, phone) VALUES (?, ?, ?, ?)")) {
            
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, role);
            ps.setString(4, phone);
            
            ps.executeUpdate();
            System.out.println("Staff added successfully!");
            
        } catch (SQLException e) {
            System.out.println("Error adding staff: " + e.getMessage());
        }
    }
    
    public static void viewAllStaff() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM staff");
             ResultSet rs = ps.executeQuery()) {
            
            System.out.printf("%-5s %-8s %-20s %-15s %-15s%n", "ID", "UserID", "Name", "Role", "Phone");
            System.out.println("-".repeat(65));
            
            while (rs.next()) {
                System.out.printf("%-5d %-8d %-20s %-15s %-15s%n",
                    rs.getInt("staff_id"), rs.getInt("user_id"), rs.getString("name"),
                    rs.getString("role"), rs.getString("phone"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing staff: " + e.getMessage());
        }
    }
}