package com.railway;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class BookTicketDB {

    // Generate 10-digit PNR
    public static String PnrGenerate() {
        Random rand = new Random();
        long number = 1000000000L + (long) (rand.nextDouble() * 9000000000L);
        return String.valueOf(number);
    }

    public static void TicketBooking(int trainNo, String travelDate, String concessionType, String classType,
                                     int passengerId, int paymentChoice) {

        try (Connection con = DBConnection.getConnection()) {
            Scanner sc = new Scanner(System.in);

            // 1. Get train_id and status
            String trainQuery = "SELECT train_id, status FROM train WHERE train_no=?";
            PreparedStatement psTrain = con.prepareStatement(trainQuery);
            psTrain.setInt(1, trainNo);
            ResultSet rsTrain = psTrain.executeQuery();
            if (!rsTrain.next()) {
                System.out.println("Train not found!");
                return;
            }
            int trainId = rsTrain.getInt("train_id");
            String trainStatus = rsTrain.getString("status");

            if (trainStatus.equalsIgnoreCase("Cancelled")) {
                System.out.println("Booking failed! Train " + trainNo + " is Cancelled.");
                return;
            }

            // 2. Get coach info
            String coachQuery = "SELECT coach_id, total_seats, fare FROM coach WHERE train_id=? AND class_type=?";
            PreparedStatement psCoach = con.prepareStatement(coachQuery);
            psCoach.setInt(1, trainId);
            psCoach.setString(2, classType);
            ResultSet rsCoach = psCoach.executeQuery();

            if (!rsCoach.next()) {
                System.out.println("No coach found for this train/class!");
                return;
            }
            int coachId = rsCoach.getInt("coach_id");
            double fare = rsCoach.getDouble("fare");
           
            // Calculate dynamic pricing
            double dynamicFare = DynamicPricing.calculateFinalPrice(trainId, classType, concessionType);
           

            // 3. Check wallet balance if needed
            double balance = 0;
            if (paymentChoice == 1) {
                String walletQuery = "SELECT balance FROM Wallet WHERE passenger_id=?";
                PreparedStatement psWallet = con.prepareStatement(walletQuery);
                psWallet.setInt(1, passengerId);
                ResultSet rsWallet = psWallet.executeQuery();
                if (!rsWallet.next()) {
                    System.out.println("Wallet not found! Add cash before booking.");
                    return;
                }
                balance = rsWallet.getDouble("balance");
                // CHANGED: Check wallet balance against dynamic fare instead of base fare
                if (balance < dynamicFare) {
                    System.out.println("Insufficient wallet balance! Required: Rs" + dynamicFare + ", Available: Rs" + balance);
                    return;
                }
            }

            // 4. Check seat availability
            String seatQuery = "SELECT seat_id, seat_no FROM seat WHERE coach_id=? AND status='Available' LIMIT 1";
            PreparedStatement psSeat = con.prepareStatement(seatQuery);
            psSeat.setInt(1, coachId);
            ResultSet rsSeat = psSeat.executeQuery();

            int seatId = 0;
            String seatNo = "";
            String bookingStatus = "";
            double finalFare = dynamicFare; // CHANGED: Use dynamic fare as final fare
            String pnr = PnrGenerate();

            if (rsSeat.next()) {
                seatId = rsSeat.getInt("seat_id");
                seatNo = rsSeat.getString("seat_no");
                bookingStatus = "Confirmed";
            } else {
                // Check RAC availability
                String racQuery = "SELECT COUNT(*) AS racCount FROM booking WHERE train_id=? AND class_type=? AND status='RAC'";
PreparedStatement psRAC = con.prepareStatement(racQuery);
psRAC.setInt(1, trainId);
psRAC.setString(2, classType);
                ResultSet rsRAC = psRAC.executeQuery();
                int racCount = 0;
                if(rsRAC.next()){
                    racCount = rsRAC.getInt("racCount");
                }
 
                if (racCount < 2) {
                    bookingStatus = "RAC";
                } else {
                    // Check WL
                    String wlQuery = "SELECT COUNT(*) AS wlCount FROM booking WHERE train_id=? AND class_type=? AND status='WL'";
PreparedStatement psWL = con.prepareStatement(wlQuery);
psWL.setInt(1, trainId);
psWL.setString(2, classType);

                   
                    ResultSet rsWL = psWL.executeQuery();
                    int wlCount = 0;
                   
                        if (rsWL.next()) {
    wlCount = rsWL.getInt("wlCount");  
}

                    if (wlCount < 1) {
                        bookingStatus = "WL";
                    } else {
                        // Ask for alternative class
                        System.out.println("All seats, RAC & WL full in this class.");
                        System.out.println("Other classes available:");
                        String altClassQuery = "SELECT class_type, fare FROM coach WHERE train_id=? AND class_type<>?";
                        PreparedStatement psAlt = con.prepareStatement(altClassQuery);
                        psAlt.setInt(1, trainId);
                        psAlt.setString(2, classType);
                        ResultSet rsAlt = psAlt.executeQuery();
                        while (rsAlt.next()) {
                            System.out.println(rsAlt.getString("class_type") + " - Fare: Rs" + rsAlt.getDouble("fare"));
                        }

                        System.out.print("Do you want to book in another class? (Yes/No): ");
                        String choice = sc.next();
                        if (choice.equalsIgnoreCase("Yes")) {
                            System.out.print("Enter class type to book: ");
                            String newClass = sc.next();
                            TicketBooking(trainNo, travelDate, concessionType, newClass, passengerId, paymentChoice);
                            return;
                        } else {
                            System.out.println("Booking cancelled.");
                            return;
                        }
                    }
                }
            }

            
            String insertBooking = "INSERT INTO booking (pnr, passenger_id, train_id, seat_id, booking_date, travel_date, status, concession_type, class_type, fare, payment_method, dynamic_fare) VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psBooking = con.prepareStatement(insertBooking);
            psBooking.setString(1, pnr);
            psBooking.setInt(2, passengerId);
            psBooking.setInt(3, trainId);
            if (seatId == 0) psBooking.setNull(4, Types.INTEGER);
            else psBooking.setInt(4, seatId);
            psBooking.setString(5, travelDate);
            psBooking.setString(6, bookingStatus);
            psBooking.setString(7, concessionType); // CHANGED: Now supports flexible concession types
            psBooking.setString(8, classType);
            psBooking.setDouble(9, fare); // Original base fare
            psBooking.setInt(10, paymentChoice);
            psBooking.setDouble(11, finalFare); // NEW: Dynamic calculated fare
            psBooking.executeUpdate();

            // 6. Deduct wallet / Paytm
            if (paymentChoice == 1) {
                String deductWallet = "UPDATE Wallet SET balance = balance - ? WHERE passenger_id=?";
                PreparedStatement psDeduct = con.prepareStatement(deductWallet);
                psDeduct.setDouble(1, finalFare);
                psDeduct.setInt(2, passengerId);
                psDeduct.executeUpdate();
                System.out.println("Payment done via Wallet. Rs" + finalFare + " deducted.");
            } else {
                System.out.print("Enter Paytm Amount: ");
                double payAmount = sc.nextDouble();
                sc.nextLine(); // clear buffer
               
                // CHANGED: Validate Paytm amount against dynamic fare
                if (payAmount < finalFare) {
                    System.out.println("Insufficient amount! Required: Rs" + finalFare + " (dynamic pricing applied)");
                    return;
                }
               
                String paytmPassword;
                while (true) {
                    System.out.print("Enter Paytm Password (4-6 digits): ");
                    paytmPassword = sc.nextLine();
                    if (Validation.isValidPaytmPassword(paytmPassword)) {
                        break;
                    }
                    System.out.println("Invalid Paytm Password! Enter 4-6 digit numeric password.");
                }
               
                System.out.println("Payment successful via Paytm. Amount: Rs" + payAmount);
                if (payAmount > finalFare) {
                    System.out.println("Change returned: Rs" + (payAmount - finalFare));
                }
            }

            // 7. Update seat if Confirmed
            if (bookingStatus.equals("Confirmed")) {
                String updateSeat = "UPDATE seat SET status='Booked', pnr=? WHERE seat_id=?";
                PreparedStatement psUpdateSeat = con.prepareStatement(updateSeat);
                psUpdateSeat.setString(1, pnr);
                psUpdateSeat.setInt(2, seatId);
                psUpdateSeat.executeUpdate();
            }

            // 8. Generate ticket text file
            Scanner s = new Scanner(System.in);
System.out.print("Ticket booked successfully! Do you want to download the ticket? (Yes/No): ");
String downloadChoice = s.next();


if (downloadChoice.equalsIgnoreCase("Yes")) {
    try (PrintWriter pw = new PrintWriter(new FileWriter("Ticket_" + pnr + ".txt"))) {
        pw.println("<======================== TICKET =====================>");
        pw.println("   PNR Number         :    " + pnr);
        pw.println("   Passenger ID       :    " + passengerId);
        pw.println("   Train Number       :    " + trainNo);
        pw.println("   Travel Date        :    " + travelDate);
        pw.println("   Class Type         :    " + classType);
        pw.println("   Concession Type    :    " + concessionType);
        pw.println("   Fare               :    Rs" + finalFare);
        pw.println("   Seat Number        :    " + ((seatId > 0) ? seatNo : bookingStatus));
        pw.println("   Booking Status     :    " + bookingStatus);
        pw.println( "<=====================================================>");
        System.out.println("Ticket file generated: Ticket_" + pnr + ".txt");
    } catch (Exception e) {
        System.out.println("Error generating ticket file: " + e.getMessage());
    }
} else {
    System.out.println("Ticket download skipped.");
}
           
        } catch (Exception e) {
            System.out.println("Error in TicketBooking: " + e.getMessage());
        }
    }
    
    public static String TicketBookingWithoutPaymentAndDownload(int trainNo, String travelDate, String concessionType, String classType, int passengerId) {
        try (Connection con = DBConnection.getConnection()) {
            String trainQuery = "SELECT train_id FROM train WHERE train_no = ? AND status = 'Running'";
            PreparedStatement psTrainId = con.prepareStatement(trainQuery);
            psTrainId.setInt(1, trainNo);
            ResultSet rsTrainId = psTrainId.executeQuery();
            
            if (!rsTrainId.next()) {
                throw new Exception("Train not found or not running");
            }
            
            int trainId = rsTrainId.getInt("train_id");
            
            String coachQuery = "SELECT coach_id, fare FROM coach WHERE train_id = ? AND class_type = ?";
            PreparedStatement psCoach = con.prepareStatement(coachQuery);
            psCoach.setInt(1, trainId);
            psCoach.setString(2, classType);
            ResultSet rsCoach = psCoach.executeQuery();
            
            if (!rsCoach.next()) {
                throw new Exception("Coach not found for this class");
            }
            
            int coachId = rsCoach.getInt("coach_id");
            double fare = rsCoach.getDouble("fare");
            
            // Calculate dynamic fare
            double dynamicFare = DynamicPricing.calculateFinalPrice(trainId, classType, concessionType);
            
            // Check seat availability
            String seatQuery = "SELECT seat_id, seat_no FROM seat WHERE coach_id=? AND status='Available' LIMIT 1";
            PreparedStatement psSeat = con.prepareStatement(seatQuery);
            psSeat.setInt(1, coachId);
            ResultSet rsSeat = psSeat.executeQuery();
            
            int seatId = 0;
            String seatNo = "";
            String bookingStatus = "";
            String pnr = PnrGenerate();
            
            if (rsSeat.next()) {
                seatId = rsSeat.getInt("seat_id");
                seatNo = rsSeat.getString("seat_no");
                bookingStatus = "Confirmed";
            } else {
                // Check RAC availability
                String racQuery = "SELECT COUNT(*) AS racCount FROM booking WHERE train_id=? AND class_type=? AND status='RAC'";
                PreparedStatement psRAC = con.prepareStatement(racQuery);
                psRAC.setInt(1, trainId);
                psRAC.setString(2, classType);
                ResultSet rsRAC = psRAC.executeQuery();
                int racCount = 0;
                if(rsRAC.next()){
                    racCount = rsRAC.getInt("racCount");
                }
                
                if (racCount < 2) {
                    bookingStatus = "RAC";
                } else {
                    // Check WL
                    String wlQuery = "SELECT COUNT(*) AS wlCount FROM booking WHERE train_id=? AND class_type=? AND status='WL'";
                    PreparedStatement psWL = con.prepareStatement(wlQuery);
                    psWL.setInt(1, trainId);
                    psWL.setString(2, classType);
                    ResultSet rsWL = psWL.executeQuery();
                    int wlCount = 0;
                    if (rsWL.next()) {
                        wlCount = rsWL.getInt("wlCount");
                    }
                    
                    if (wlCount < 1) {
                        bookingStatus = "WL";
                    } else {
                        bookingStatus = "WL"; // Default to WL if all full
                    }
                }
            }
            
            // Insert booking
            String insertBooking = "INSERT INTO booking (pnr, passenger_id, train_id, seat_id, booking_date, travel_date, status, concession_type, class_type, fare, payment_method, dynamic_fare) VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, 1, ?)";
            PreparedStatement psBooking = con.prepareStatement(insertBooking);
            psBooking.setString(1, pnr);
            psBooking.setInt(2, passengerId);
            psBooking.setInt(3, trainId);
            if (seatId == 0) psBooking.setNull(4, Types.INTEGER);
            else psBooking.setInt(4, seatId);
            psBooking.setString(5, travelDate);
            psBooking.setString(6, bookingStatus);
            psBooking.setString(7, concessionType);
            psBooking.setString(8, classType);
            psBooking.setDouble(9, fare);
            psBooking.setDouble(10, dynamicFare);
            psBooking.executeUpdate();
            
            // Update seat if confirmed
            if (bookingStatus.equals("Confirmed") && seatId > 0) {
                String updateSeat = "UPDATE seat SET status='Booked', pnr=? WHERE seat_id=?";
                PreparedStatement psUpdateSeat = con.prepareStatement(updateSeat);
                psUpdateSeat.setString(1, pnr);
                psUpdateSeat.setInt(2, seatId);
                psUpdateSeat.executeUpdate();
            }
            
            return pnr;
            
        } catch (Exception e) {
            throw new RuntimeException("Booking failed: " + e.getMessage());
        }
    }
    
    public static void generateTicketFile(String pnr) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT b.*, t.train_no, s.seat_no FROM booking b " +
                        "LEFT JOIN train t ON b.train_id = t.train_id " +
                        "LEFT JOIN seat s ON b.seat_id = s.seat_id " +
                        "WHERE b.pnr = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, pnr);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                try (PrintWriter pw = new PrintWriter(new FileWriter("Ticket_" + pnr + ".txt"))) {
                    pw.println("<======================== TICKET =====================>");
                    pw.println("   PNR Number         :    " + pnr);
                    pw.println("   Passenger ID       :    " + rs.getInt("passenger_id"));
                    pw.println("   Train Number       :    " + rs.getInt("train_no"));
                    pw.println("   Travel Date        :    " + rs.getString("travel_date"));
                    pw.println("   Class Type         :    " + rs.getString("class_type"));
                    pw.println("   Concession Type    :    " + rs.getString("concession_type"));
                    pw.println("   Fare               :    Rs" + rs.getDouble("dynamic_fare"));
                    
                    String seatNo = rs.getString("seat_no");
                    String status = rs.getString("status");
                    pw.println("   Seat Number        :    " + (seatNo != null ? seatNo : status));
                    pw.println("   Booking Status     :    " + status);
                    pw.println("<=====================================================>");
                }
            }
        } catch (Exception e) {
            System.out.println("Error generating ticket file: " + e.getMessage());
        }
    }
}