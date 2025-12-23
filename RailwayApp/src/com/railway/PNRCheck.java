package com.railway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PNRCheck {

    public static void checkByPassengerId(int passengerId) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT b.pnr, t.train_no, t.name, b.travel_date, b.status, s.seat_no, b.concession_type, b.class_type, b.fare, b.dynamic_fare " +
                         "FROM booking b " +
                         "LEFT JOIN train t ON b.train_id=t.train_id " +
                         "LEFT JOIN seat s ON b.seat_id=s.seat_id " +
                         "WHERE b.passenger_id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, passengerId);
            ResultSet rs = ps.executeQuery();

            boolean hasBookings = false;
            System.out.println("\n<================ Your Bookings ================>");
            while(rs.next()){
                hasBookings = true;
                String seatNo = rs.getString("seat_no");
                String seatDisplay = (seatNo != null) ? seatNo : "N/A";
                String travelDate = rs.getString("travel_date");
                double baseFare = rs.getDouble("fare");
                double dynamicFare = rs.getDouble("dynamic_fare");
                String concession = rs.getString("concession_type");
                
                System.out.println("\n--- Booking Details ---");
                System.out.printf("PNR Number    : %s%n", rs.getString("pnr"));
                System.out.printf("Train         : %d - %s%n", rs.getInt("train_no"), rs.getString("name"));
                System.out.printf("Travel Date   : %s%n", travelDate);
                System.out.printf("Class         : %s%n", rs.getString("class_type"));
                System.out.printf("Seat Number   : %s%n", seatDisplay);
                System.out.printf("Status        : %s%n", rs.getString("status"));
                
                if (concession != null && !concession.equals("null")) {
                    System.out.printf("Concession    : %s%n", concession);
                }
                
                if (dynamicFare > 0 && dynamicFare != baseFare) {
                    System.out.printf("Base Fare     : ₹%.2f%n", baseFare);
                    System.out.printf("Final Fare    : ₹%.2f%n", dynamicFare);
                } else {
                    System.out.printf("Fare          : ₹%.2f%n", baseFare);
                }
                System.out.println("-------------------------------");
            }
            if(!hasBookings) {
                System.out.println("\n No bookings found for your account.");
                System.out.println(" Book your first ticket to see booking history here!");
                System.out.println("===============================================");
            }
        } catch(Exception e){
            System.out.println(" Unable to load booking history. Please try again later.");
        }
    }

    public static void checkByPNR(String pnr) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT b.pnr, t.train_no, t.name, b.travel_date, b.status, s.seat_no, b.concession_type, b.class_type, b.fare, b.dynamic_fare " +
                         "FROM booking b " +
                         "LEFT JOIN train t ON b.train_id=t.train_id " +
                         "LEFT JOIN seat s ON b.seat_id=s.seat_id " +
                         "WHERE b.pnr=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, pnr);
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                String seatNo = rs.getString("seat_no");
                String seatDisplay = (seatNo != null) ? seatNo : "N/A";
                String travelDate = rs.getString("travel_date");
                
                double baseFare = rs.getDouble("fare");
                double dynamicFare = rs.getDouble("dynamic_fare");

                System.out.println("\n<================ Booking Details ================>");
                System.out.printf("PNR Number    : %s%n", rs.getString("pnr"));
                System.out.printf("Train         : %d - %s%n", rs.getInt("train_no"), rs.getString("name"));
                System.out.printf("Travel Date   : %s%n", travelDate);
                System.out.printf("Class         : %s%n", rs.getString("class_type"));
                System.out.printf("Seat Number   : %s%n", seatDisplay);
                System.out.printf("Status        : %s%n", rs.getString("status"));
                
                String concession = rs.getString("concession_type");
                if (concession != null && !concession.equals("null")) {
                    System.out.printf("Concession    : %s%n", concession);
                }
                
                if (dynamicFare > 0 && dynamicFare != baseFare) {
                    System.out.printf("Base Fare     : ₹%.2f%n", baseFare);
                    System.out.printf("Final Fare    : ₹%.2f%n", dynamicFare);
                } else {
                    System.out.printf("Fare          : ₹%.2f%n", baseFare);
                }
                System.out.println("================================================");
            } else {
                System.out.println(" PNR not found! Please check your PNR number and try again.");
                System.out.println(" Make sure you entered the correct 10-digit PNR number.");
            }
        } catch(Exception e){
            System.out.println(" Unable to check PNR status. Please try again later.");
        }
    }
}
