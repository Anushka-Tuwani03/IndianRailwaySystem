package com.railway;

import java.sql.*;

public class Coach {
    int coachId, trainId, totalSeats;
    String classType;
    double fare; 

    public void addCoach() {
        String sql = "INSERT INTO Coach (train_id, class_type, total_seats, fare) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, trainId);
            stmt.setString(2, classType);
            stmt.setInt(3, totalSeats);

            if (fare <= 0) {
                fare = switch (classType.toLowerCase()) {
                    case "ac" -> 500.0;
                    case "sleeper" -> 250.0;
                    case "general" -> 100.0;
                    default -> 100.0;
                };
            }
            stmt.setDouble(4, fare);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                coachId = rs.getInt(1);
            }

            System.out.println("Coach added successfully with ID: " + coachId);

            createSeats(conn);
            addDefaultPricing(conn);

        } catch (SQLException e) {
            System.out.println("Error adding coach: " + e.getMessage());
        }
    }

    private void createSeats(Connection conn) throws SQLException {
        String seatSql = "INSERT INTO Seat (coach_id, seat_no, status) VALUES (?, ?, 'Available')";
        try (PreparedStatement ps = conn.prepareStatement(seatSql)) {
            for (int i = 1; i <= totalSeats; i++) {
                ps.setInt(1, coachId);
                ps.setString(2, classType.charAt(0) + String.valueOf(i)); 
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println(totalSeats + " seats created for Coach ID: " + coachId);
    }

    private void addDefaultPricing(Connection conn) throws SQLException {
        String priceSql = "INSERT INTO Pricing (train_id, class_type, base_fare, tatkal_multiplier, demand_multiplier) VALUES (?, ?, ?, 1.5, 1.0)";
        try (PreparedStatement ps = conn.prepareStatement(priceSql)) {
            ps.setInt(1, trainId);
            ps.setString(2, classType);
            ps.setDouble(3, fare); 
            ps.executeUpdate();
        }
        System.out.println("Default pricing added for Coach class: " + classType + " with fare: " + fare);
    }
}
