package com.railway;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.Random;
import java.util.Scanner;

public class TatkalBooking {
public static void bookTatkalTicket(int passengerId) {
    Scanner k = new Scanner(System.in);
    String pnr = null;
    int trainNo = 0;
    String travelDate = null;
    String classType = null;
    double fare = 0;
    String bookingStatus = null;
    String seatNo = null;
    int seatId = 0;

    try (Connection con = DBConnection.getConnection()) {

        while (true) {
            try {
                System.out.print("Enter Train Number (5 digits): ");
                trainNo = k.nextInt();
                
                if (trainNo < 10000 || trainNo > 99999) {
                    System.out.println(" Train number must be exactly 5 digits (e.g., 12345).");
                    continue;
                }
                
                String checkTrainSql = "SELECT COUNT(*) FROM train WHERE train_no = ?";
                PreparedStatement checkPs = con.prepareStatement(checkTrainSql);
                checkPs.setInt(1, trainNo);
                ResultSet checkRs = checkPs.executeQuery();
                
                if (checkRs.next() && checkRs.getInt(1) > 0) {
                    break;
                } else {
                    System.out.println("Train number not found in our database!");
                    System.out.println("Please enter a valid 5-digit train number from our train schedule.");
                    continue;
                }
            } catch (java.util.InputMismatchException e) {
                System.out.println("Please enter a valid 5-digit train number (numbers only).");
                k.nextLine();
            } catch (SQLException e) {
                System.out.println("Unable to verify train number. Please try again.");
            }
        }
        
        while (true) {
            try {
                System.out.print("Enter Travel Date (yyyy-MM-dd): ");
                travelDate = k.next();
                
                if (!isValidDateFormat(travelDate)) {
                    System.out.println("Invalid date format! Please use yyyy-MM-dd format (e.g., 2024-01-15).");
                    continue;
                }
                break;
            } catch (Exception e) {
                System.out.println("Unable to read travel date. Please try again.");
            }
        }

        LocalDate travel = LocalDate.parse(travelDate);
        if (!travel.equals(LocalDate.now().plusDays(1))) {
            System.out.println("Tatkal booking allowed only 1 day before travel.");
            return;
        }

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
            System.out.println("Train is Cancelled!");
            return;
        }

        System.out.println("Select Class Type:");
        System.out.println("1. AC");
        System.out.println("2. Sleeper");
        System.out.println("3. General");
        
        int type;
        while (true) {
            try {
                System.out.print("Enter choice (1-3): ");
                type = k.nextInt();
                if (type >= 1 && type <= 3) {
                    break;
                } else {
                    System.out.println("Please select a valid option (1, 2, or 3).");
                }
            } catch (java.util.InputMismatchException e) {
                System.out.println("Please enter a valid number (1, 2, or 3).");
                k.nextLine(); 
                try {
                    Thread.sleep(100); 
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        classType = switch (type) {
            case 1 -> "AC";
            case 2 -> "Sleeper";
            case 3 -> "General";
            default -> null;
        };

        String quotaQuery = "SELECT tatkal_seats FROM quota WHERE train_id=?";
        PreparedStatement psQuota = con.prepareStatement(quotaQuery);
        psQuota.setInt(1, trainId);
        ResultSet rsQuota = psQuota.executeQuery();
        int tatkalAvailable = 0;
        if (rsQuota.next()) tatkalAvailable = rsQuota.getInt("tatkal_seats");

        if (tatkalAvailable <= 0) {
            System.out.println("No Tatkal seats available!");
            return;
        }

        fare = DynamicPricing.calculateFinalPrice(trainId, classType, null);
        System.out.println("Tatkal fare (with dynamic pricing) for " + classType + ": Rs" + fare);
        System.out.println("Select Payment Option:");
        System.out.println("1. Wallet");
        System.out.println("2. Paytm");
        
        int paymentChoice;
        while (true) {
            try {
                System.out.print("Enter choice (1 or 2): ");
                paymentChoice = k.nextInt();
                if (paymentChoice == 1 || paymentChoice == 2) {
                    break;
                } else {
                    System.out.println("Please select 1 (Wallet) or 2 (Paytm).");
                }
            } catch (java.util.InputMismatchException e) {
                System.out.println("Please enter a valid number (1 or 2).");
                k.nextLine();
            }
        }

        if(paymentChoice == 1) {
            String walletQuery = "SELECT balance FROM wallet WHERE passenger_id=?";
            PreparedStatement psWallet = con.prepareStatement(walletQuery);
            psWallet.setInt(1, passengerId);
            ResultSet rsWallet = psWallet.executeQuery();
            if(rsWallet.next()) {
                double balance = rsWallet.getDouble("balance");
                if(balance < fare) {
                    System.out.println("Insufficient wallet balance! Add cash first.");
                    return;
                } else {
                    String deduct = "UPDATE wallet SET balance = balance - ? WHERE passenger_id=?";
                    PreparedStatement psDeduct = con.prepareStatement(deduct);
                    psDeduct.setDouble(1, fare);
                    psDeduct.setInt(2, passengerId);
                    psDeduct.executeUpdate();
                    System.out.println("Payment done via Wallet. Rs" + fare + " deducted.");
                }
            } else {
                System.out.println("Wallet not found!");
                return;
            }
        } else {
            System.out.println("Paytm selected. Payment successful.");
        }

        String confirm;
        while (true) {
            try {
                System.out.print("Confirm Tatkal Booking? (Yes/No): ");
                confirm = k.next();
                if (confirm.equalsIgnoreCase("Yes") || confirm.equalsIgnoreCase("No")) {
                    break;
                } else {
                    System.out.println("Please enter 'Yes' or 'No'.");
                }
            } catch (Exception e) {
                System.out.println("Unable to read confirmation. Please try again.");
            }
        }
        if (!confirm.equalsIgnoreCase("Yes")) {
            System.out.println("Tatkal booking cancelled.");
            return;
        }

        String seatQuery = "SELECT seat_id, seat_no FROM seat WHERE coach_id=(SELECT coach_id FROM coach WHERE train_id=? AND class_type=?) AND status='Available' LIMIT 1";
        PreparedStatement psSeat = con.prepareStatement(seatQuery);
        psSeat.setInt(1, trainId);
        psSeat.setString(2, classType);
        ResultSet rsSeat = psSeat.executeQuery();

        bookingStatus = "Confirmed";

        if (rsSeat.next()) {
            seatId = rsSeat.getInt("seat_id");
            seatNo = rsSeat.getString("seat_no");
        } else {
            String racQuery = "SELECT COUNT(*) AS racCount FROM booking WHERE train_id=? AND class_type=? AND status='RAC'";
            PreparedStatement psRAC = con.prepareStatement(racQuery);
            psRAC.setInt(1, trainId);
            psRAC.setString(2, classType);
            ResultSet rsRAC = psRAC.executeQuery();
            int racCount = 0;
            if (rsRAC.next()) racCount = rsRAC.getInt("racCount");
            if (racCount < 2) {
                bookingStatus = "RAC";
            } else {
                String wlQuery = "SELECT COUNT(*) AS wlCount FROM booking WHERE train_id=? AND class_type=? AND status='WL'";
                PreparedStatement psWL = con.prepareStatement(wlQuery);
                psWL.setInt(1, trainId);
                psWL.setString(2, classType);
                ResultSet rsWL = psWL.executeQuery();
                int wlCount = 0;
                if (rsWL.next()) wlCount = rsWL.getInt("wlCount");
                if (wlCount < 1) {
                    bookingStatus = "WL";
                } else {
                    System.out.println("All seats, RAC & WL full in this class. Booking cancelled.");
                    return;
                }
            }
        }

        pnr = String.valueOf(1000000000L + new Random().nextInt(900000000));
        String insertBooking = "INSERT INTO booking(pnr, passenger_id, train_id, seat_id, booking_date, travel_date, status, class_type, fare, booking_type, dynamic_fare) VALUES(?, ?, ?, ?, NOW(), ?, ?, ?, ?, 'Tatkal', ?)";
        PreparedStatement psBook = con.prepareStatement(insertBooking);
        psBook.setString(1, pnr);
        psBook.setInt(2, passengerId);
        psBook.setInt(3, trainId);
        if(seatId > 0) psBook.setInt(4, seatId);
        else psBook.setNull(4, Types.INTEGER);
        psBook.setString(5, travelDate);
        psBook.setString(6, bookingStatus);
        psBook.setString(7, classType);
        psBook.setDouble(8, 0); 
        psBook.setDouble(9, fare); 
        psBook.executeUpdate();

        String updateQuota = "UPDATE quota SET tatkal_seats=tatkal_seats-1 WHERE train_id=?";
        PreparedStatement psUpdateQuota = con.prepareStatement(updateQuota);
        psUpdateQuota.setInt(1, trainId);
        psUpdateQuota.executeUpdate();

        if (seatId > 0 && bookingStatus.equals("Confirmed")) {
            String updateSeat = "UPDATE seat SET status='Booked', pnr=? WHERE seat_id=?";
            PreparedStatement psUpdateSeat = con.prepareStatement(updateSeat);
            psUpdateSeat.setString(1, pnr);
            psUpdateSeat.setInt(2, seatId);
            psUpdateSeat.executeUpdate();
        }

        System.out.println("Tatkal Ticket Booked! PNR: " + pnr + " Fare: Rs" + fare + " Status: " + bookingStatus);
        
        String downloadChoice;
        while (true) {
            try {
                System.out.print("Do you want to download the ticket? (Yes/No): ");
                downloadChoice = k.next();
                if (downloadChoice.equalsIgnoreCase("Yes") || downloadChoice.equalsIgnoreCase("No")) {
                    break;
                } else {
                    System.out.println("Please enter 'Yes' or 'No'.");
                }
            } catch (Exception e) {
                System.out.println("Unable to read download choice. Please try again.");
            }
        }
        
        if (downloadChoice.equalsIgnoreCase("Yes")) {
            try(PrintWriter pw = new PrintWriter(new FileWriter("Ticket_" + pnr + ".txt"))) {
                pw.println("<==============INDIAN RAILWAYS RESERVATION SYSTEM==============>");
                pw.println("   PNR Number       : " + pnr);
                pw.println("   Passenger ID     : " + passengerId);
                pw.println("   Train Number     : " + trainNo);
                pw.println("   Travel Date      : " + travelDate);
                pw.println("   Class Type       : " + classType);
                pw.println("   Fare             : Rs" + fare);
                pw.println("   Booking Type     : Tatkal");
                pw.println("   Status           : " + bookingStatus);
                pw.println("   Seat Number      : " + ((seatId > 0) ? seatNo : bookingStatus));
                pw.println("             *** Have a Safe & Happy Journey! ***");
                pw.println("<=====================================================>");
                System.out.println("Ticket file generated: Ticket_" + pnr + ".txt");
            } catch(Exception e){
                System.out.println("Error generating ticket: " + e.getMessage());
            }
        } else {
            System.out.println("Ticket download skipped.");
        }

    } catch (Exception e) {
        System.out.println("Tatkal booking service is currently unavailable. Please try again later.");
    }
}
    private static boolean isValidDateFormat(String date) {
        try {
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return false;
            }
            
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            
            if (year < 2024 || year > 2025) return false;
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;
            
            if (month == 2) {
                if (day > 29) return false;
            } else if (month == 4 || month == 6 || month == 9 || month == 11) {
                if (day > 30) return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
