package com.railway;

import java.sql.*;
import java.util.Scanner;

public class DynamicPricing {
    private static Scanner sc = new Scanner(System.in);
    
    public static double calculateFinalPrice(int trainId, String classType, String concessionType) {
        double baseFare = getBaseFare(trainId, classType);
        double occupancyMultiplier = getOccupancyMultiplier(trainId, classType);
        double finalPrice = baseFare * occupancyMultiplier;
        finalPrice = applyConcession(finalPrice, concessionType);
        return Math.round(finalPrice * 100.0) / 100.0;
    }
    
    private static double getBaseFare(int trainId, String classType) {
        String sql = "SELECT fare FROM coach WHERE train_id = ? AND class_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setString(2, classType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("fare");
            }
        } catch (SQLException e) {
            System.out.println("Error loading base fare: " + e.getMessage());
        }
        return 0;
    }
    
    private static double getOccupancyMultiplier(int trainId, String classType) {
        try (Connection conn = DBConnection.getConnection()) {
            String totalSql = "SELECT total_seats FROM Coach WHERE train_id = ? AND class_type = ?";
            PreparedStatement totalPs = conn.prepareStatement(totalSql);
            totalPs.setInt(1, trainId);
            totalPs.setString(2, classType);
            ResultSet totalRs = totalPs.executeQuery();
            
            if (totalRs.next()) {
                int totalSeats = totalRs.getInt("total_seats");
                
                String bookedSql = "SELECT COUNT(*) as booked FROM Seat s " +
                                  "JOIN Coach c ON s.coach_id = c.coach_id " +
                                  "WHERE c.train_id = ? AND c.class_type = ? AND s.status = 'Booked'";
                PreparedStatement bookedPs = conn.prepareStatement(bookedSql);
                bookedPs.setInt(1, trainId);
                bookedPs.setString(2, classType);
                ResultSet bookedRs = bookedPs.executeQuery();
                
                if (bookedRs.next()) {
                    int bookedSeats = bookedRs.getInt("booked");
                    double occupancyRate = (double) bookedSeats / totalSeats;
                    
                    if (occupancyRate >= 0.6) {
                        return 1.2;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error calculating occupancy: " + e.getMessage());
        }
        return 1.0;
    }
    
    private static double getTatkalMultiplier(int trainId, String classType) {
        return 1.0;
    }
    
    private static double getDemandMultiplier(int trainId, String classType) {
        return 1.0;
    }
    
    private static double applyConcession(double price, String concessionType) {
        if (concessionType == null) return price;
        
        switch (concessionType.toLowerCase()) {
            case "senior citizen":
            case "differently abled":
            case "army":
                return price * 0.8;
            default:
                return price;
        }
    }
    
    public static void adminPricingMenu() {
        while (true) {
            System.out.println("\n=== Dynamic Pricing Management ===");
            System.out.println("1. Update Tatkal Multiplier");
            System.out.println("2. Update Demand Multiplier");
            System.out.println("3. View Current Pricing");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");
            
            int choice = sc.nextInt();
            sc.nextLine();
            
            switch (choice) {
                case 1:
                    updateTatkalMultiplier();
                    break;
                case 2:
                    updateDemandMultiplier();
                    break;
                case 3:
                    viewCurrentPricing();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    private static void updateTatkalMultiplier() {
        System.out.print("Enter Train ID: ");
        int trainId = sc.nextInt();
        System.out.print("Enter Class Type: ");
        String classType = sc.next();
        System.out.print("Enter Tatkal Multiplier (e.g., 1.5 for 50% increase): ");
        double multiplier = sc.nextDouble();
        
        String sql = "UPDATE Pricing SET tatkal_multiplier = ? WHERE train_id = ? AND class_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, multiplier);
            ps.setInt(2, trainId);
            ps.setString(3, classType);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Tatkal multiplier updated successfully!");
            } else {
                System.out.println("Train/Class not found!");
            }
        } catch (SQLException e) {
            System.out.println("Error updating tatkal multiplier: " + e.getMessage());
        }
    }
    
    private static void updateDemandMultiplier() {
        System.out.print("Enter Train ID: ");
        int trainId = sc.nextInt();
        System.out.print("Enter Class Type: ");
        String classType = sc.next();
        System.out.print("Enter Demand Multiplier (e.g., 1.3 for 30% increase): ");
        double multiplier = sc.nextDouble();
        
        String sql = "UPDATE Pricing SET demand_multiplier = ? WHERE train_id = ? AND class_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, multiplier);
            ps.setInt(2, trainId);
            ps.setString(3, classType);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Demand multiplier updated successfully!");
            } else {
                System.out.println("Train/Class not found!");
            }
        } catch (SQLException e) {
            System.out.println("Error updating demand multiplier: " + e.getMessage());
        }
    }
    
    private static void viewCurrentPricing() {
        String sql = "SELECT p.train_id, t.train_no, t.name, p.class_type, p.base_fare, " +
                    "p.tatkal_multiplier, p.demand_multiplier FROM Pricing p " +
                    "JOIN Train t ON p.train_id = t.train_id ORDER BY p.train_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            System.out.printf("%-8s %-12s %-20s %-10s %-10s %-10s %-10s%n",
                    "TrainID", "TrainNo", "Name", "Class", "BaseFare", "Tatkal", "Demand");
            System.out.println("--------------------------------------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-8d %-12s %-20s %-10s %-10.2f %-10.2f %-10.2f%n",
                        rs.getInt("train_id"),
                        rs.getString("train_no"),
                        rs.getString("name"),
                        rs.getString("class_type"),
                        rs.getDouble("base_fare"),
                        rs.getDouble("tatkal_multiplier"),
                        rs.getDouble("demand_multiplier"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing pricing: " + e.getMessage());
        }
    }
}