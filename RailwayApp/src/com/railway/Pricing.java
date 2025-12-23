package com.railway;

import java.sql.*;

public class Pricing {
    int trainId;
    String classType;
    double baseFare;
    double tatkalMultiplier = 1.5;   // default
    double demandMultiplier = 1.0;   // default

    // View all current prices
    public void viewCurrentPrices() {
        String sql = "SELECT p.train_id, t.train_no, t.name, p.class_type, p.base_fare, p.tatkal_multiplier, p.demand_multiplier " +
                     "FROM Pricing p JOIN Train t ON p.train_id = t.train_id ORDER BY p.train_id, p.class_type";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.printf("%-10s %-12s %-20s %-10s %-12s %-12s %-12s%n",
                    "Train ID", "Train No", "Train Name", "Class", "Base Fare", "Tatkal", "Demand");
            System.out.println("-------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-10d %-12s %-20s %-10s %-12.2f %-12.2f %-12.2f%n",
                        rs.getInt("train_id"),
                        rs.getString("train_no"),
                        rs.getString("name"),
                        rs.getString("class_type"),
                        rs.getDouble("base_fare"),
                        rs.getDouble("tatkal_multiplier"),
                        rs.getDouble("demand_multiplier"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing prices: " + e.getMessage());
        }
    }
   
    // Add new pricing (checks for duplicate)
public void addPricing() {
    try (Connection conn = DBConnection.getConnection()) {
        // Check if pricing exists
        String checkSql = "SELECT * FROM Pricing WHERE train_id = ? AND class_type = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, trainId);
        checkStmt.setString(2, classType);
        ResultSet rs = checkStmt.executeQuery();

            String sql = "INSERT INTO Pricing (train_id, class_type, base_fare, tatkal_multiplier, demand_multiplier) " +
                         "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, trainId);
            ps.setString(2, classType);
            ps.setDouble(3, baseFare);
            ps.setDouble(4, tatkalMultiplier);
            ps.setDouble(5, demandMultiplier);
            ps.executeUpdate();
            System.out.println("Pricing added successfully for Train ID: " + trainId + ", Class: " + classType);

            //  Coach table update
            String updateCoach = "UPDATE Coach SET fare=? WHERE train_id=? AND class_type=?";
            PreparedStatement psCoach = conn.prepareStatement(updateCoach);
            psCoach.setDouble(1, baseFare);
            psCoach.setInt(2, trainId);
            psCoach.setString(3, classType);
            int updated = psCoach.executeUpdate();

            if (updated > 0) {
                System.out.println("Coach fare updated successfully for Train ID: " + trainId + ", Class: " + classType);
            } else {
                System.out.println(" No matching coach found to update fare.");
            }
       // }
    } catch (SQLException e) {
        System.out.println("Error adding pricing: " + e.getMessage());
    }
}


    // Update base fare for a specific train/class
public void updateBaseFare() {
    String sql = "UPDATE Pricing SET base_fare = ? WHERE train_id = ? AND class_type = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setDouble(1, baseFare);
        ps.setInt(2, trainId);
        ps.setString(3, classType);

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("Base fare updated successfully in Pricing table!");

            //  Coach table update
            String updateCoach = "UPDATE Coach SET fare=? WHERE train_id=? AND class_type=?";
            PreparedStatement psCoach = conn.prepareStatement(updateCoach);
            psCoach.setDouble(1, baseFare);
            psCoach.setInt(2, trainId);
            psCoach.setString(3, classType);
            psCoach.executeUpdate();

            System.out.println("Coach fare also updated successfully!");
        } else {
            System.out.println("Train/Class combination not found!");
        }
    } catch (SQLException e) {
        System.out.println("Error updating fare: " + e.getMessage());
    }
}


    // Update tatkal multiplier for all trains/classes or a specific class
    public void updateTatkalMultiplier() {
        String sql = "UPDATE Pricing SET tatkal_multiplier = ? WHERE class_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, tatkalMultiplier);
            ps.setString(2, classType); // pass null or specific class
            int rows = ps.executeUpdate();
            System.out.println("Tatkal multiplier updated for " + rows + " entries.");
        } catch (SQLException e) {
            System.out.println("Error updating tatkal multiplier: " + e.getMessage());
        }
    }

    // Update demand multiplier for a specific train
    public void updateDemandMultiplier() {
        String sql = "UPDATE Pricing SET demand_multiplier = ? WHERE train_id = ? AND class_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, demandMultiplier);
            ps.setInt(2, trainId);
            ps.setString(3, classType);

            int rows = ps.executeUpdate();
            System.out.println("Demand multiplier updated for " + rows + " entries.");
        } catch (SQLException e) {
            System.out.println("Error updating demand multiplier: " + e.getMessage());
        }
    }
}
