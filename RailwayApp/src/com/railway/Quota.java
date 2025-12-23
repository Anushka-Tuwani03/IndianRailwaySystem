package com.railway;

import java.sql.*;

public class Quota {
    int quotaId, trainId, generalSeats, tatkalSeats, ladiesSeats, seniorCitizenSeats;

    public void allocateQuota() {
        String sql = "INSERT INTO Quota (train_id, general_seats, tatkal_seats, ladies_seats, senior_citizen_seats) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, trainId);
            stmt.setInt(2, generalSeats);
            stmt.setInt(3, tatkalSeats);
            stmt.setInt(4, ladiesSeats);
            stmt.setInt(5, seniorCitizenSeats);

            stmt.executeUpdate();
            System.out.println("Quota allocated successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}