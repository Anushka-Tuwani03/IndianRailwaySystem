package com.railway;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class CancelTicket {
    public static Scanner k = new Scanner(System.in);

    public static void cancelTicket(int passengerId) {
        try (Connection con = DBConnection.getConnection()) {
            System.out.println(">> You selected: Cancel Ticket");
            
            String pnr;
            while (true) {
                try {
                    System.out.print("Enter PNR to cancel (or '0' to go back): ");
                    pnr = k.next();
                    
                    if (pnr.equals("0")) {
                        System.out.println("Returning to dashboard...");
                        return;
                    }
                    
                    // Validate PNR format
                    if (!Validation.isValidPNR(pnr)) {
                        System.out.println(" Invalid PNR format! PNR must be exactly 10 digits (e.g., 1234567890).");
                        continue;
                    }
                    
                    break; // Valid PNR format, exit loop
                } catch (Exception e) {
                    System.out.println(" Unable to read PNR. Please try again.");
                }
            }

            // Fetch booking details
            try {
                String sqlBooking = "SELECT travel_date, fare, dynamic_fare, status, seat_id, class_type, train_id, payment_method FROM booking WHERE pnr=? AND passenger_id=?";
                PreparedStatement ps = con.prepareStatement(sqlBooking);
                ps.setString(1, pnr);
                ps.setInt(2, passengerId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    System.out.println(" PNR not found in your bookings!");
                    System.out.println(" Please check your PNR number or make sure this booking belongs to your account.");
                    return;
                }

            String status = rs.getString("status");
            double fare = rs.getDouble("fare");
            double dynamicFare = rs.getDouble("dynamic_fare");
            int seatId = rs.getInt("seat_id");
            int trainId = rs.getInt("train_id");
            String classType = rs.getString("class_type");
            int paymentMethod = rs.getInt("payment_method");
            
            double actualFare = (dynamicFare > 0) ? dynamicFare : fare;

            if (status.equalsIgnoreCase("Cancelled")) {
                // Check cancellation details
                String cancelQuery = "SELECT cancelled_at, refund_amount FROM cancellation WHERE pnr=?";
                PreparedStatement psCancelInfo = con.prepareStatement(cancelQuery);
                psCancelInfo.setString(1, pnr);
                ResultSet rsCancelInfo = psCancelInfo.executeQuery();
                
                if (rsCancelInfo.next()) {
                    Timestamp cancelledAt = rsCancelInfo.getTimestamp("cancelled_at");
                    double refundAmount = rsCancelInfo.getDouble("refund_amount");
                    System.out.println(" This ticket was already cancelled on: " + cancelledAt);
                    System.out.println(" Refund amount: ₹" + refundAmount);
                } else {
                    System.out.println(" This ticket is already cancelled.");
                }
                return;
            }

            // Fetch train departure datetime
            String sqlTrain = "SELECT departure_time FROM train WHERE train_id=?";
            PreparedStatement psTrain = con.prepareStatement(sqlTrain);
            psTrain.setInt(1, trainId);
            ResultSet rsTrain = psTrain.executeQuery();

            LocalDateTime departureDateTime = LocalDateTime.now(); // default fallback
            if (rsTrain.next()) {
                Timestamp dep = rsTrain.getTimestamp("departure_time");
                if (dep != null) departureDateTime = dep.toLocalDateTime();
            }

            // Calculate hours left
            long hoursLeft = ChronoUnit.HOURS.between(LocalDateTime.now(), departureDateTime);

            // UPDATED: New refund policy
            double refund = 0;
            if (status.equals("RAC")) {
                refund = actualFare; // RAC gets full refund
            } else if (status.equals("Confirmed")) {
                if (hoursLeft < 0) {
                    refund = 0; // Train departed - no refund
                } else if (hoursLeft >= 24) {
                    refund = actualFare; // 24+ hours - full refund
                } else {
                    refund = actualFare * 0.7; // Within 24 hours - 70% refund
                }
            }

            // Update booking status
            String updateBooking = "UPDATE booking SET status='Cancelled' WHERE pnr=?";
            PreparedStatement psUpdate = con.prepareStatement(updateBooking);
            psUpdate.setString(1, pnr);
            psUpdate.executeUpdate();

            // Insert cancellation record
            String insertCancel = "INSERT INTO cancellation(pnr, cancelled_at, refund_amount) VALUES (?, NOW(), ?)";
            PreparedStatement psCancel = con.prepareStatement(insertCancel);
            psCancel.setString(1, pnr);
            psCancel.setDouble(2, refund);
            psCancel.executeUpdate();

            // Update seat if booked
            if (seatId > 0) {
                String updateSeat = "UPDATE seat SET status='Available', pnr=NULL WHERE seat_id=?";
                PreparedStatement psSeat = con.prepareStatement(updateSeat);
                psSeat.setInt(1, seatId);
                psSeat.executeUpdate();

                // Promote RAC → Confirmed
                String racQuery = "SELECT pnr, passenger_id, fare, payment_method FROM booking WHERE train_id=? AND class_type=? AND status='RAC' ORDER BY booking_date ASC LIMIT 1";
                PreparedStatement psRac = con.prepareStatement(racQuery);
                psRac.setInt(1, trainId);
                psRac.setString(2, classType);
                ResultSet rsRac = psRac.executeQuery();

                if (rsRac.next()) {
                    String racPnr = rsRac.getString("pnr");
                    int racPassengerId = rsRac.getInt("passenger_id");
                    double racFare = rsRac.getDouble("fare");
                    int racPaymentMethod = rsRac.getInt("payment_method");

                    // Assign seat
                    String updateRacSeat = "UPDATE booking SET status='Confirmed', seat_id=? WHERE pnr=?";
                    PreparedStatement psUpdateRac = con.prepareStatement(updateRacSeat);
                    psUpdateRac.setInt(1, seatId);
                    psUpdateRac.setString(2, racPnr);
                    psUpdateRac.executeUpdate();

                    String seatUpdate = "UPDATE seat SET status='Booked', pnr=? WHERE seat_id=?";
                    PreparedStatement psSeatUpdate = con.prepareStatement(seatUpdate);
                    psSeatUpdate.setString(1, racPnr);
                    psSeatUpdate.setInt(2, seatId);
                    psSeatUpdate.executeUpdate();

                    if (racPaymentMethod == 1) {
                        String deductWallet = "UPDATE Wallet SET balance = balance - ? WHERE passenger_id=?";
                        PreparedStatement psWallet = con.prepareStatement(deductWallet);
                        psWallet.setDouble(1, racFare);
                        psWallet.setInt(2, racPassengerId);
                        psWallet.executeUpdate();
                        System.out.println("RAC passenger promoted to Confirmed. PNR: " + racPnr + ". Wallet deducted ₹" + racFare);
                    } else {
                        System.out.println("RAC passenger PNR " + racPnr + " will pay via Paytm.");
                    }
                }
            }

            if (refund > 0) {
                String checkWallet = "SELECT COUNT(*) FROM Wallet WHERE passenger_id=?";
                PreparedStatement psCheck = con.prepareStatement(checkWallet);
                psCheck.setInt(1, passengerId);
                ResultSet rsCheck = psCheck.executeQuery();
                
                if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                    // Create wallet if doesn't exist
                    String createWallet = "INSERT INTO Wallet (passenger_id, balance) VALUES (?, 0)";
                    PreparedStatement psCreate = con.prepareStatement(createWallet);
                    psCreate.setInt(1, passengerId);
                    psCreate.executeUpdate();
                    System.out.println("Wallet created for passenger ID: " + passengerId);
                }
                
                String updateWallet = "UPDATE Wallet SET balance = balance + ? WHERE passenger_id=?";
                PreparedStatement psWallet = con.prepareStatement(updateWallet);
                psWallet.setDouble(1, refund);
                psWallet.setInt(2, passengerId);
                int rowsUpdated = psWallet.executeUpdate();
                
                if (rowsUpdated > 0) {
                    System.out.println("Refund Rs" + String.format("%.0f", refund) + " added to Wallet.");
                } else {
                    System.out.println("Failed to add refund to wallet. Please contact support.");
                }
            } 

                System.out.println(" Ticket cancelled successfully. Refund Amount: Rs" + refund);
            } catch (SQLException e) {
                System.out.println(" Database error during cancellation. Please try again later.");
            } catch (Exception e) {
                System.out.println(" Unable to process cancellation. Please try again later.");
            }

        } catch (SQLException e) {
            System.out.println(" Unable to connect to booking system. Please try again later.");
        } catch (Exception e) {
            System.out.println(" Cancellation service is currently unavailable. Please try again later.");
        }
    }
}
