package com.railway;

import java.sql.*;

public class LiveStatus {
    int trainId, platformNo;
    String delayInfo; 
    boolean cancellationFlag;

    public void updateLiveStatus() {
        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false); 

            String checkSql = "SELECT live_id FROM livestatus WHERE train_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, trainId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String updateSql = "UPDATE livestatus SET delay_info=?, platform_no=?, cancellation_flag=? WHERE train_id=?";
                PreparedStatement psUpdate = conn.prepareStatement(updateSql);
                psUpdate.setString(1, delayInfo);
                psUpdate.setInt(2, platformNo);
                psUpdate.setBoolean(3, cancellationFlag);
                psUpdate.setInt(4, trainId);
                psUpdate.executeUpdate();
                System.out.println("Live status updated successfully!");
            } else {
                String insertSql = "INSERT INTO livestatus (train_id, delay_info, platform_no, cancellation_flag) VALUES (?, ?, ?, ?)";
                PreparedStatement psInsert = conn.prepareStatement(insertSql);
                psInsert.setInt(1, trainId);
                psInsert.setString(2, delayInfo);
                psInsert.setInt(3, platformNo);
                psInsert.setBoolean(4, cancellationFlag);
                psInsert.executeUpdate();
                System.out.println("Live status added successfully!");
            }

            String trainStatus = cancellationFlag ? "Cancelled" : "Running";
            String trainUpdateSql = "UPDATE train SET status=? WHERE train_id=?";
            PreparedStatement psTrain = conn.prepareStatement(trainUpdateSql);
            psTrain.setString(1, trainStatus);
            psTrain.setInt(2, trainId);
            psTrain.executeUpdate();

            if (!cancellationFlag && delayInfo != null && !delayInfo.isEmpty()) {
                int delayMinutes = extractDelayMinutes(delayInfo);
                if (delayMinutes > 0) {
                    String updateTimeSql = "UPDATE train SET departure_time = DATE_ADD(departure_time, INTERVAL ? MINUTE), " +
                                          "arrival_time = DATE_ADD(arrival_time, INTERVAL ? MINUTE) WHERE train_id=?";
                    PreparedStatement psTime = conn.prepareStatement(updateTimeSql);
                    psTime.setInt(1, delayMinutes);
                    psTime.setInt(2, delayMinutes);
                    psTime.setInt(3, trainId);
                    int rowsUpdated = psTime.executeUpdate();
                    
                    if (rowsUpdated > 0) {
                        System.out.println("Train timings updated successfully! Delayed by " + delayMinutes + " minutes.");
                    } else {
                        System.out.println("Failed to update train timings. Train ID not found.");
                    }
                }
            }


            String fetchPassengers = "SELECT passenger_id, fare FROM booking WHERE train_id=? AND status IN ('Confirmed','RAC')";
            PreparedStatement psPassengers = conn.prepareStatement(fetchPassengers);
            psPassengers.setInt(1, trainId);
            ResultSet rsPassengers = psPassengers.executeQuery();

            while (rsPassengers.next()) {
                int passengerId = rsPassengers.getInt("passenger_id");
                double fare = rsPassengers.getDouble("fare");
                String message = "";
if (cancellationFlag) {

    String trainInfoSql = "SELECT train_no, name FROM train WHERE train_id = ?";
    PreparedStatement psTrainInfo = conn.prepareStatement(trainInfoSql);
    psTrainInfo.setInt(1, trainId);
    ResultSet rsTrainInfo = psTrainInfo.executeQuery();
    
    String trainDetails = "Train " + trainId;
    if (rsTrainInfo.next()) {
        trainDetails = "Train " + rsTrainInfo.getString("train_no") + " (" + rsTrainInfo.getString("name") + ")";
    }
    
    message = " CANCELLED: " + trainDetails + ". Reason: " + delayInfo + ". Full refund processed to your wallet.";


    String updateWallet = "UPDATE wallet SET balance = balance + ? WHERE passenger_id=?";
    PreparedStatement psWallet = conn.prepareStatement(updateWallet);
    psWallet.setDouble(1, fare);
    psWallet.setInt(2, passengerId);
    psWallet.executeUpdate();

} else if (delayInfo != null && !delayInfo.isEmpty()) {

    String trainInfoSql = "SELECT train_no, name, departure_time FROM train WHERE train_id = ?";
    PreparedStatement psTrainInfo = conn.prepareStatement(trainInfoSql);
    psTrainInfo.setInt(1, trainId);
    ResultSet rsTrainInfo = psTrainInfo.executeQuery();
    
    String trainDetails = "Train " + trainId;
    String newDepartureTime = "";
    if (rsTrainInfo.next()) {
        trainDetails = "Train " + rsTrainInfo.getString("train_no") + " (" + rsTrainInfo.getString("name") + ")";
        newDepartureTime = rsTrainInfo.getString("departure_time");
    }
    
    int delayMinutes = extractDelayMinutes(delayInfo);
    if (delayMinutes > 0) {
        message = " DELAYED: " + trainDetails + " delayed by " + delayMinutes + " minutes. New departure time: " + newDepartureTime + ". Reason: " + delayInfo.replaceAll("\\d+", "").trim();
    } else {
        message = "ℹ UPDATE: " + trainDetails + " - " + delayInfo;
    }
}


                if (!message.isEmpty()) {
                    String insertNotification = "INSERT INTO notifications(passenger_id, message) VALUES(?,?)";
                    PreparedStatement psNotify = conn.prepareStatement(insertNotification);
                    psNotify.setInt(1, passengerId);
                    psNotify.setString(2, message);
                    psNotify.executeUpdate();
                }
            }


            if (cancellationFlag) {

                String updateBooking = "UPDATE booking SET status='Cancelled', seat_id=NULL WHERE train_id=?";
                PreparedStatement psBooking = conn.prepareStatement(updateBooking);
                psBooking.setInt(1, trainId);
                psBooking.executeUpdate();


                String deleteSeats = "DELETE FROM seat WHERE coach_id IN (SELECT coach_id FROM coach WHERE train_id=?)";
                PreparedStatement psSeats = conn.prepareStatement(deleteSeats);
                psSeats.setInt(1, trainId);
                psSeats.executeUpdate();


                String deleteCoaches = "DELETE FROM coach WHERE train_id=?";
                PreparedStatement psCoaches = conn.prepareStatement(deleteCoaches);
                psCoaches.setInt(1, trainId);
                psCoaches.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                Connection conn = DBConnection.getConnection();
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    private int extractDelayMinutes(String delayInfo) {
        try {
            if (delayInfo == null || delayInfo.trim().isEmpty()) {
                return 0;
            }
            

            String[] parts = delayInfo.trim().split("\\s+");
            for (int i = parts.length - 1; i >= 0; i--) {
                try {

                    int minutes = Integer.parseInt(parts[i]);
                    if (minutes > 0 && minutes <= 1440) {
                        return minutes;
                    }
                } catch (NumberFormatException e) {

                    continue;
                }
            }
            

            return 0;
        } catch (Exception e) {
            System.out.println("Error parsing delay info: " + e.getMessage());
            return 0;
        }
    }
    

    public static void displayLiveStatus(int trainId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT t.train_no, t.name, t.source, t.destination, " +
                        "t.departure_time, t.arrival_time, t.status, " +
                        "ls.delay_info, ls.platform_no, ls.cancellation_flag " +
                        "FROM train t LEFT JOIN livestatus ls ON t.train_id = ls.train_id " +
                        "WHERE t.train_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, trainId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                System.out.println("\n<================ Live Train Status ================>");
                System.out.println("Train No: " + rs.getString("train_no"));
                System.out.println("Train Name: " + rs.getString("name"));
                System.out.println("Route: " + rs.getString("source") + " → " + rs.getString("destination"));
                System.out.println("Departure Time: " + rs.getString("departure_time"));
                System.out.println("Arrival Time: " + rs.getString("arrival_time"));
                System.out.println("Status: " + rs.getString("status"));
                
                String delayInfo = rs.getString("delay_info");
                if (delayInfo != null && !delayInfo.isEmpty()) {
                    System.out.println("Delay Info: " + delayInfo);
                }
                
                int platformNo = rs.getInt("platform_no");
                if (platformNo > 0) {
                    System.out.println("Platform: " + platformNo);
                }
                
                boolean cancelled = rs.getBoolean("cancellation_flag");
                if (cancelled) {
                    System.out.println(" TRAIN CANCELLED ");
                }
                
                System.out.println("===================================================\n");
            } else {
                System.out.println("Train not found!");
            }
            
        } catch (SQLException e) {
            System.out.println("Error displaying live status: " + e.getMessage());
        }
    }
}
