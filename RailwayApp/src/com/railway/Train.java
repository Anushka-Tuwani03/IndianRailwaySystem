package com.railway;

import java.sql.*;
import java.time.LocalDateTime;

public class Train {
    int trainId;
    String trainNo;
    String name;
    String source;
    String destination;
    String departureTime;
    String arrivalTime;
    String status = "Running";

    public void addTrain() {
        String sql = "INSERT INTO Train (train_no, name, source, destination, departure_time, arrival_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, trainNo);
            stmt.setString(2, name);
            stmt.setString(3, source);
            stmt.setString(4, destination);
            stmt.setTimestamp(5, Timestamp.valueOf(departureTime));
            stmt.setTimestamp(6, Timestamp.valueOf(arrivalTime));
            stmt.setString(7, status);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                trainId = rs.getInt(1);
            }

            System.out.println("Train added successfully with ID: " + trainId);

        } catch (SQLException e) {
            System.out.println("Error adding train: " + e.getMessage());
        }
    }

    public void cancelTrain() {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            String statusSql = "UPDATE Train SET status='Cancelled' WHERE train_id=?";
            try (PreparedStatement ps = conn.prepareStatement(statusSql)) {
                ps.setInt(1, trainId);
                ps.executeUpdate();
            }

            String bookingSql = "SELECT pnr, passenger_id, fare, status FROM Booking WHERE train_id=? AND status IN ('Confirmed','RAC','WL')";
            try (PreparedStatement ps = conn.prepareStatement(bookingSql)) {
                ps.setInt(1, trainId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String pnr = rs.getString("pnr");
                    int passengerId = rs.getInt("passenger_id");
                    double fare = rs.getDouble("fare");
                    String oldStatus = rs.getString("status");

                    String updateBooking = "UPDATE Booking SET status='Cancelled' WHERE pnr=?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateBooking)) {
                        psUpdate.setString(1, pnr);
                        psUpdate.executeUpdate();
                    }

                    if (!oldStatus.equals("WL")) {
                        String refundSql = "UPDATE Wallet SET balance = balance + ? WHERE passenger_id=?";
                        try (PreparedStatement psRefund = conn.prepareStatement(refundSql)) {
                            psRefund.setDouble(1, fare);
                            psRefund.setInt(2, passengerId);
                            psRefund.executeUpdate();
                        }
                    }

                    String cancelInsert = "INSERT INTO Cancellation (pnr, cancelled_at, refund_amount) VALUES (?, NOW(), ?)";
                    try (PreparedStatement psCancel = conn.prepareStatement(cancelInsert)) {
                        psCancel.setString(1, pnr);
                        psCancel.setDouble(2, fare);
                        psCancel.executeUpdate();
                    }
                }
            }

            String seatSql = "DELETE s FROM Seats s JOIN Coach c ON s.coach_id = c.coach_id WHERE c.train_id=?";
            try (PreparedStatement ps = conn.prepareStatement(seatSql)) {
                ps.setInt(1, trainId);
                ps.executeUpdate();
            }

            String coachSql = "DELETE FROM Coach WHERE train_id=?";
            try (PreparedStatement ps = conn.prepareStatement(coachSql)) {
                ps.setInt(1, trainId);
                ps.executeUpdate();
            }

            String pricingSql = "DELETE FROM Pricing WHERE train_id=?";
            try (PreparedStatement ps = conn.prepareStatement(pricingSql)) {
                ps.setInt(1, trainId);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Train cancelled successfully! Bookings updated and refunds processed.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error cancelling train. Transaction rolled back.");
        }
    }

    public void addPricing(String coachClass, double fare) {
        try (Connection conn = DBConnection.getConnection()) {
            String coachSql = "UPDATE Coach SET fare=? WHERE train_id=? AND class_type=?";
            try (PreparedStatement ps = conn.prepareStatement(coachSql)) {
                ps.setDouble(1, fare);
                ps.setInt(2, trainId);
                ps.setString(3, coachClass);
                ps.executeUpdate();
            }

            Pricing pricing = new Pricing();
            pricing.trainId = trainId;
            pricing.classType = coachClass;
            pricing.baseFare = fare;
            pricing.addPricing();

            System.out.println("Pricing added/updated for Train ID: " + trainId + ", Class: " + coachClass);

        } catch (SQLException e) {
            System.out.println("Error updating pricing: " + e.getMessage());
        }
    }
}