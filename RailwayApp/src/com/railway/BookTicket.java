
package com.railway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

 class BookTicket {
    public static Scanner k = new Scanner(System.in);

   
    public static void book(int passengerId) {
        startBooking(passengerId);
    }

    static class TicketRequest {
        int trainNum;
        String travelDate;
        String classType;
        String concessionType;
        double fare;
    }

    public static void startBooking(int passengerId) {
        System.out.print("How many tickets do you want to book? ");
        int totalTickets = k.nextInt();
        k.nextLine();

        List<TicketRequest> tickets = new ArrayList<>();
        double totalFare = 0;
        
        int previousTrainNum = 0;
        String previousTravelDate = "";

        for (int i = 1; i <= totalTickets; i++) {
            System.out.println("\n<================= Booking Ticket " + i + " =================>");
            
            boolean useSameTrainDate = false;
            if (i > 1) {
                System.out.print("Do you want to book tickets in the same train & date? (Yes/No): ");
                String choice = k.nextLine();
                useSameTrainDate = choice.equalsIgnoreCase("Yes");
            }
            
            TicketRequest tr = collectTicketDetails(useSameTrainDate, previousTrainNum, previousTravelDate);
            if (tr == null) {
                System.out.println(" Ticket " + i + " cancelled.");
                continue;
            }
            
            if (i == 1) {
                previousTrainNum = tr.trainNum;
                previousTravelDate = tr.travelDate;
            }
            
            tickets.add(tr);
            totalFare += tr.fare;
        }

        if (tickets.isEmpty()) {
            System.out.println("\n⚠ No tickets to book. Returning...");
            return;
        }

        System.out.println("\n<========= Final Payment =========>");
        System.out.println("Total Tickets: " + tickets.size());
        System.out.println("Total Fare   : Rs " + String.format("%.0f", totalFare));

        int paymentChoice = -1;
        while (paymentChoice == -1) {
            System.out.println("Select Payment Option:");
            System.out.println("1. Wallet");
            System.out.println("2. Paytm");
            System.out.println("0. Cancel All Bookings");

            try {
                System.out.print("Choice: ");
                int choice = k.nextInt();
                if (choice == 0) {
                    System.out.println("Booking cancelled.");
                    return;
                } else if (choice == 1 || choice == 2) {
                    paymentChoice = choice;
                } else {
                    System.out.println("Please enter a valid number (0,1,2).");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number (0,1,2).");
                k.nextLine();
            }
        }

        System.out.print("Confirm booking of all tickets? (Yes/No): ");
        String confirm = k.next();
        if (!confirm.equalsIgnoreCase("Yes")) {
            System.out.println(" Booking cancelled.");
            return;
        }

        if (!processSinglePayment(totalFare, paymentChoice, passengerId)) {
            System.out.println(" Payment failed. Booking cancelled.");
            return;
        }
        
        List<String> pnrList = new ArrayList<>();
        for (int i = 0; i < tickets.size(); i++) {
            TicketRequest tr = tickets.get(i);
            try {
                String pnr = BookTicketDB.TicketBookingWithoutPaymentAndDownload(
                        tr.trainNum,
                        tr.travelDate,
                        tr.concessionType,
                        tr.classType,
                        passengerId
                );
                pnrList.add(pnr);
                System.out.println("Ticket " + (i + 1) + " booked successfully! PNR: " + pnr);
            } catch (Exception e) {
                System.out.println("Failed to book ticket " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        System.out.println("\n--- Download Tickets ---");
        for (int i = 0; i < pnrList.size(); i++) {
            System.out.print("Download Ticket " + (i + 1) + "? (Yes/No): ");
            String download = k.next();
            if (download.equalsIgnoreCase("Yes")) {
                BookTicketDB.generateTicketFile(pnrList.get(i));
                System.out.println("📄 Ticket " + (i + 1) + " downloaded: Ticket_" + pnrList.get(i) + ".txt");
            }
        }

        System.out.println("\n All tickets booked successfully!");
    }

    // Single ticket details
    private static TicketRequest collectTicketDetails() {
        return collectTicketDetails(false, 0, "");
    }
    
    private static TicketRequest collectTicketDetails(boolean useSameTrainDate, int previousTrainNum, String previousTravelDate) {
        TicketRequest tr = new TicketRequest();

        if (useSameTrainDate && previousTrainNum > 0) {
            // Use previous train and date
            tr.trainNum = previousTrainNum;
            tr.travelDate = previousTravelDate;
            System.out.println("Using same train: " + previousTrainNum + " and date: " + previousTravelDate);
        } else {
            // Train number
            while (true) {
                try {
                    System.out.print("Enter Train Number (5 digits): ");
                    tr.trainNum = k.nextInt();
                    k.nextLine(); // Clear buffer
                    if (tr.trainNum < 10000 || tr.trainNum > 99999) {
                        System.out.println("Train number must be exactly 5 digits.");
                        continue;
                    }
                    if (isTrainExists(tr.trainNum)) break;
                    else System.out.println("Train not found in database!");
                } catch (InputMismatchException e) {
                    System.out.println("Please enter a valid 5-digit train number.");
                    k.nextLine();
                }
            }

            // Travel date
            while (true) {
                System.out.print("Enter Travel Date (yyyy-MM-dd): ");
                tr.travelDate = k.nextLine();
                if (!isValidDateFormat(tr.travelDate)) {
                    System.out.println("Invalid date format!");
                    continue;
                }
                if (!isDateAvailableForTrain(tr.trainNum, tr.travelDate)) {
                    System.out.println("Train not running on this date!");
                    continue;
                }
                break;
            }
        }
        while(true){

        // Show available classes for this train
        showAvailableClasses(tr.trainNum);
        
        // Class type
        System.out.println("Select Class Type:");
        System.out.println("1. AC");
        System.out.println("2. Sleeper");
        System.out.println("3. General");
        System.out.println("0. Cancel Ticket");
        System.out.print("Choose class type :- ");
        int type = k.nextInt();
        k.nextLine(); // Clear buffer
        switch (type) {
            case 1 -> tr.classType = "AC";
            case 2 -> tr.classType = "Sleeper";
            case 3 -> tr.classType = "General";
            case 0 -> { return null; }
            default -> { System.out.println("Invalid choice.");continue; }
        }
    if(isClassAvailableForTrain(tr.trainNum,tr.classType)){
        break;
    }
    else{
        System.out.println("No coach available for "+tr.classType+" .Please select another class.");
    }
}
    
        // Concession
        System.out.println("Select Concession Type:");
        System.out.println("1. None");
        System.out.println("2. Senior Citizen");
        System.out.println("3. Army");
        System.out.println("4. Differently Abled");
        //new
        System.out.print(" Enter option :- ");
        int c = k.nextInt();
        k.nextLine(); // Clear buffer
        tr.concessionType = switch(c) {
            case 1 -> null;
            case 2 -> "senior citizen";
            case 3 -> "army";
            case 4 -> "differently abled";
            default -> null;
        };

        // Fare calculation
        int trainId = getTrainIdFromTrainNo(tr.trainNum);
        if (trainId == -1) {
            System.out.println(" Train ID not found for train number " + tr.trainNum);
            return null;
        }
        tr.fare = DynamicPricing.calculateFinalPrice(trainId, tr.classType, tr.concessionType);
        
        if (tr.fare <= 0) {
            System.out.println(" Invalid fare calculated. Train/Class combination not available.");
            return null;
        }
        
        System.out.println("Calculated Fare: Rs " + String.format("%.0f", tr.fare));
        if (tr.concessionType != null) {
            System.out.println("Concession Applied: " + tr.concessionType + " (20% discount)");
        }

        return tr;
    }

    // Check if train exists
    private static boolean isTrainExists(int trainNum) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM train WHERE train_no = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, trainNum);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) { return false; }
    }

    // Date format validation
    private static boolean isValidDateFormat(String date) {
        return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    // Check if train runs on date
    private static boolean isDateAvailableForTrain(int trainNum, String date) {
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM Train WHERE train_no = ? AND DATE(departure_time) = ? AND status = 'Running'";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, trainNum);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) { return false; }
    }

    private static boolean processSinglePayment(double totalFare, int paymentChoice, int passengerId) {
        if (paymentChoice == 1) {
            // Wallet payment
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT balance FROM wallet WHERE passenger_id = ?");
                ps.setInt(1, passengerId);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    if (balance >= totalFare) {
                        PreparedStatement psUpdate = con.prepareStatement("UPDATE wallet SET balance = balance - ? WHERE passenger_id = ?");
                        psUpdate.setDouble(1, totalFare);
                        psUpdate.setInt(2, passengerId);
                        psUpdate.executeUpdate();
                        System.out.printf(" Payment successful via Wallet. Rs%.0f deducted.%n", totalFare);
                        return true;
                    } else {
                        System.out.printf("Insufficient wallet balance! Required: Rs%.0f, Available: Rs%.0f%n", totalFare, balance);
                        return false;
                    }
                } else {
                    System.out.println(" Wallet not found!");
                    return false;
                }
            } catch (Exception e) {
                System.out.println(" Payment failed.");
                return false;
            }
        } else {
            // Paytm payment
            System.out.printf("Enter Paytm Amount: ");
            double payAmount = k.nextDouble();
            k.nextLine();
            
            if (payAmount < totalFare) {
                System.out.printf(" Insufficient amount! Required: Rs%.0f%n", totalFare);
                return false;
            }
            
            System.out.print("Enter Paytm Password (4-6 digits): ");
            String password = k.nextLine();
            
            if (Validation.isValidPaytmPassword(password)) {
                System.out.printf(" Payment successful via Paytm. Amount: Rs%.0f%n", payAmount);
                if (payAmount > totalFare) {
                    System.out.printf(" Change returned: Rs%.0f%n", (payAmount - totalFare));
                }
                return true;
            } else {
                System.out.println("Invalid Paytm password!");
                return false;
            }
        }
    }

    // Fetch train_id from train_no
    private static int getTrainIdFromTrainNo(int trainNum) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT train_id FROM train WHERE train_no = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, trainNum);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("train_id");
        } catch (Exception e) {
            System.out.println(" Error fetching train_id for train_no: " + trainNum);
        }
        return -1;
    }

    private static void showAvailableClasses(int trainNum) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT DISTINCT c.class_type, c.fare FROM coach c JOIN train t ON c.train_id = t.train_id WHERE t.train_no = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, trainNum);
            ResultSet rs = ps.executeQuery();
            
            System.out.println("\n=== Available Classes for Train " + trainNum + " ===");
            boolean hasClasses = false;
            while (rs.next()) {
                hasClasses = true;
                System.out.println("- " + rs.getString("class_type") + " (Fare: Rs" + rs.getDouble("fare") + ")");
            }
            
            if (!hasClasses) {
                System.out.println("No classes available for this train!");
            }
            System.out.println("==========================================\n");
        } catch (Exception e) {
            System.out.println("Unable to fetch available classes.");
        }
    }
    private static boolean isClassAvailableForTrain(int trainNum, String classType) {
    try (Connection con = DBConnection.getConnection()) {
        String sql = "SELECT COUNT(*) FROM coach c JOIN train t ON c.train_id = t.train_id " +
                     "WHERE t.train_no = ? AND c.class_type = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, trainNum);
        ps.setString(2, classType);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    } catch (Exception e) {
        return false;
    }
}
 }