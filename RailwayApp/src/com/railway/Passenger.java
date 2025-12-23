package com.railway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Passenger {
    public static Scanner k = new Scanner(System.in);
    public static int loggedPassengerId;

    // Main dashboard for logged-in passenger
    public static void optionPassenger(int userId) {
        try {
            loggedPassengerId = PassengerDB.getPassengerIdByUserId(userId);
            if (loggedPassengerId == -1) {
                System.out.println(" Unable to access your account. Please login again.");
                return;
            }
        } catch (Exception e) {
            System.out.println(" Something went wrong while accessing your account. Please try again.");
            return;
        }

        boolean f = true;
        while(f) {
            try {
                System.out.println("<================ Passenger Dashboard ================>");
                System.out.println("Welcome To The page!!");
                System.out.println("Please choose a Service :- ");
                System.out.println(" 1. Search Train");
                System.out.println(" 2. Book Ticket");
                System.out.println(" 3. Check Pnr Status");
                System.out.println(" 4. Wallet");  
                System.out.println(" 5. Cancel Ticket");
                System.out.println(" 6. File Complaint");
                System.out.println(" 7. Notifications");
                System.out.println(" 8. Tatkal booking");
                System.out.println(" 9. Exit");
                System.out.print("Enter option :- ");
                
                int p;
                try {
                    p = k.nextInt();
                    if (p < 1 || p > 9) {
                        System.out.println("Invalid choice! Please select a number between 1-9.");
                        continue;
                    }
                } catch (java.util.InputMismatchException e) {
                    System.out.println("Please enter a valid number (1-9).");
                    k.nextLine();
                    continue;
                }

                switch(p) {
                    case 1: 
                        try {
                            SearchTrain(); 
                        } catch (Exception e) {
                            System.out.println(" Unable to search trains right now. Please try again later.");
                        }
                        break;
                    case 2: 
                        try {
                            BookTicket.book(loggedPassengerId); 
                        } catch (Exception e) {
                            System.out.println(" Ticket booking is currently unavailable. Please try again later.");
                        }
                        break;
                    case 3: 
                        try {
                            int checkChoice = -1;
                            while (checkChoice == -1) {
                                System.out.println(">> You selected: Check PNR Status");
                                System.out.println("Choose an option:");
                                System.out.println("1. Check specific ticket by PNR");
                                System.out.println("2. View all booking history");
                                System.out.println("0. Back to Dashboard");
                                System.out.print("Enter choice: ");
                                
                                try {
                                    int choice = k.nextInt();
                                    if (choice >= 0 && choice <= 2) {
                                        checkChoice = choice;
                                    } else {
                                        System.out.println("Please enter a valid option (0, 1, or 2).");
                                    }
                                } catch (java.util.InputMismatchException e) {
                                    System.out.println("Please enter a valid option (0, 1, or 2).");
                                    k.nextLine();
                                }
                            }
                            
                            switch(checkChoice){
                                case 1:
                                    try {
                                        String pnr;
                                        while (true) {
                                            System.out.print("Enter PNR: ");
                                            pnr = k.next();
                                            
                                            // Validate PNR format
                                            if (!Validation.isValidPNR(pnr)) {
                                                System.out.println("Invalid PNR format! PNR must be exactly 10 digits (e.g., 1234567890).");
                                                continue;
                                            }
                                            break;
                                        }
                                        
                                        PNRCheck.checkByPNR(pnr);
                                    } catch (Exception e) {
                                        System.out.println("Unable to check PNR status. Please try again later.");
                                    }
                                    break;
                                case 2:
                                    try {
                                        PNRCheck.checkByPassengerId(loggedPassengerId);
                                    } catch (Exception e) {
                                        System.out.println(" Unable to load your booking history. Please try again later.");
                                    }
                                    break;
                                case 0:
                                    System.out.println("Returning to dashboard...");
                                    break;
                                default:
                                    System.out.println("Invalid choice. Returning to dashboard.");
                            }
                        } catch (Exception e) {
                            System.out.println(" PNR service is temporarily unavailable. Please try again later.");
                        }
                        break;
                    case 4: 
                        try {
                            walletMenu(); 
                        } catch (Exception e) {
                            System.out.println(" Wallet service is currently unavailable. Please try again later.");
                        }
                        break;
                    case 5:
                        try {
                            CancelTicket.cancelTicket(loggedPassengerId);
                        } catch (Exception e) {
                            System.out.println(" Unable to cancel ticket right now. Please try again later.");
                        }
                        break;
                    case 6:
                        try {
                            fileComplaint(loggedPassengerId);
                        } catch (Exception e) {
                            System.out.println(" Unable to file complaint right now. Please try again later.");
                        }
                        break;
                    case 7:
                        try {
                            showNotifications();
                        } catch (Exception e) {
                            System.out.println(" Unable to load notifications. Please try again later.");
                        }
                        break;
                    case 8:
                        try {
                            TatkalBooking.bookTatkalTicket(loggedPassengerId);
                        } catch (Exception e) {
                            System.out.println(" Tatkal booking is currently unavailable. Please try again later.");
                        }
                        break;
                    case 9:
                        System.out.println("Exiting Passenger Dashboard..."); 
                        f=false; 
                        break;
                    default: 
                        System.out.println("Please choose Valid option..."); 
                        break;
                }
            } catch (Exception e) {
                System.out.println(" Something went wrong. Returning to main menu...");
                break;
            }
        }
    }
    public static void showNotifications() {
    try (Connection con = DBConnection.getConnection()) {
        String sql = "SELECT message, created_at, is_read FROM notifications WHERE passenger_id=? ORDER BY created_at DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, loggedPassengerId);
        ResultSet rs = ps.executeQuery();

        boolean hasMsg = false;
        System.out.println("\n<================ Notifications ================>");
        while (rs.next()) {
            hasMsg = true;
           // Correct type for timestamp
            java.sql.Timestamp time = rs.getTimestamp("created_at");
            String message = rs.getString("message");
            int isRead = rs.getInt("is_read");
             String readStatus = (isRead == 0) ? "Unread" : "Read";
            System.out.printf("[%s] %s - %s%n", time.toString(), readStatus, message);
           // Mark as read after displaying
            if (isRead == 0) {
                String updateRead = "UPDATE notifications SET is_read=1 WHERE passenger_id=? AND created_at=?";
                PreparedStatement psUpdate = con.prepareStatement(updateRead);
                psUpdate.setInt(1, loggedPassengerId);
                psUpdate.setTimestamp(2, time);
                psUpdate.executeUpdate();
            }
        }
        if (!hasMsg) {
            System.out.println("No notifications at the moment.");
        }
    } catch (Exception e) {
        System.out.println(" Unable to load your notifications. Please try again later."+ e.getMessage());
    }
}
   public static void fileComplaint(int passengerId) {
    try (Connection con = DBConnection.getConnection()) {
        Scanner sc = new Scanner(System.in);
       String pnr;
        while (true) {
            System.out.print("Enter your PNR (or '0' to go back): ");
            pnr = sc.nextLine();
            
            if (pnr.equals("0")) {
                System.out.println("Returning to dashboard...");
                return;
            }
           
            if (!Validation.isValidPNR(pnr)) {
                System.out.println("Invalid PNR format! PNR must be exactly 10 digits (e.g., 1234567890).");
                continue;
            }
             
            String checkPnrSql = "SELECT COUNT(*) FROM booking b join train t on b.train_id = t.train_id WHERE b.pnr = ? AND passenger_id = ? and b.status = 'Confirmed' and t.departure_time <=NOW()";
            PreparedStatement checkPs = con.prepareStatement(checkPnrSql);
            checkPs.setString(1, pnr);
            checkPs.setInt(2, passengerId);
            ResultSet checkRs = checkPs.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                break; 
            } else {
                System.out.println("Please enter a valid PNR to file complaint .");
                continue;
            }
        }


        System.out.print("Enter your complaint: ");
        String complaintText = sc.nextLine();
       System.out.println(" Enter date :- ");
       
        
        if (complaintText.trim().isEmpty()) {
            System.out.println("Complaint cannot be empty. Please try again.");
            return;
        }


        // Complaint insert karna
        String sql = "INSERT INTO complaints (pnr, complaint_text, status) VALUES (?, ?, 'Pending' )";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, pnr);
        ps.setString(2, complaintText);

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("Complaint filed successfully! Our support team will review it soon.");
        } else {
            System.out.println("Failed to file complaint. Please try again.");
        }

    } catch (Exception e) {
        System.out.println("Unable to submit your complaint right now. Please try again later.");
    }
}
      // Wallet submenu
    public static void walletMenu() {
        boolean w = true;
        while(w) {
            try {
                System.out.println("\n<================ Wallet Menu ================>");
                System.out.println(" 1. View Balance");
                System.out.println(" 2. Add Cash");
                System.out.println(" 3. Back");
                System.out.print("Enter choice :- ");
                int c;
                try {
                    c = k.nextInt();
                    if (c < 1 || c > 3) {
                        System.out.println("Invalid choice! Please select 1, 2, or 3.");
                        continue;
                    }
                } catch (java.util.InputMismatchException e) {
                    System.out.println("Please enter a valid number (1, 2, or 3).");
                    k.nextLine();
                    continue;
                }
                switch(c){
                    case 1: 
                        try {
                            viewBalance(); 
                        } catch (Exception e) {
                            System.out.println(" Unable to check your wallet balance. Please try again later.");
                        }
                        break;
                    case 2: 
                        try {
                            addCash(); 
                        } catch (Exception e) {
                            System.out.println(" Unable to add money to wallet. Please try again later.");
                        }
                        break;
                    case 3: w=false; break;
                    default: System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println(" Wallet service temporarily unavailable. Returning to dashboard...");
                break;
            }
        }
    }
     public static void viewBalance() {
        try(Connection con = DBConnection.getConnection()) {
            String sql = "SELECT balance FROM Wallet WHERE passenger_id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, loggedPassengerId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) System.out.println(">> Current Wallet Balance: ₹" + rs.getDouble("balance"));
            else System.out.println(">> Wallet not found!");
        } catch(Exception e) {
            System.out.println(" Unable to check your balance right now. Please try again later.");
        }
    }
      public static void addCash() {
        try {
            System.out.print("Enter amount to add :- ");
            double amount;
            try {
                amount = k.nextDouble();
                if (amount <= 0) {
                    System.out.println("Please enter an amount greater than 0.");
                    return;
                }
                if (amount > 100000) {
                    System.out.println("Maximum amount limit is Rs.100000. Please enter a smaller amount.");
                    return;
                }
            } catch (java.util.InputMismatchException e) {
                System.out.println("Please enter a valid amount (numbers only).");
                k.nextLine();
                return;
            }
             try(Connection con = DBConnection.getConnection()) {
                String sql = "UPDATE Wallet SET balance=balance+? WHERE passenger_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setDouble(1, amount);
                ps.setInt(2, loggedPassengerId);
                int rows = ps.executeUpdate();
                if(rows>0) System.out.println(">> ₹"+amount+" added to wallet successfully!");
                else System.out.println(">> Wallet not found!");
            } catch(Exception e) {
                System.out.println(" Unable to add money to your wallet. Please try again later.");
            }
        } catch (Exception e) {
            System.out.println(" Money transfer failed. Please try again later.");
        }
    }
      // Train search
    public static void SearchTrain() {
        try {
            System.out.println(">> You selected: Search Trains");
            System.out.println(">>Please enter Source, Destination, and Date of Journey.");
            String sourcefrom;
            while (true) {
                System.out.print("Source from (or '0' to go back): ");
                try {
                    sourcefrom = k.next();
                    if (sourcefrom.equals("0")) {
                        System.out.println("Returning to dashboard...");
                        return;
                    }
                    if (!Validation.isValidStationName(sourcefrom)) {
                        System.out.println("Invalid source station. Please enter a valid station name (letters only).");
                        continue;
                    }
                     // Check if station exists in database
                    try (Connection con = DBConnection.getConnection()) {
                        PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM train WHERE source = ? OR destination = ?");
                        ps.setString(1, sourcefrom);
                        ps.setString(2, sourcefrom);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            System.out.println("Station not found in our network. Please enter a valid station.");
                            continue;
                        }
                    } catch (Exception e) {
                        // If DB check fails, continue anyway
                    }
                    
                    // Check if source station exists in database
                    try (Connection con = DBConnection.getConnection()) {
                        PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM train WHERE LOWER(source) = LOWER(?) OR LOWER(destination) = LOWER(?)");
                        ps.setString(1, sourcefrom);
                        ps.setString(2, sourcefrom);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            System.out.println("Source station '" + sourcefrom + "' not found in our railway network. Please enter a valid station.");
                            continue;
                        }
                    } catch (Exception e) {
                        System.out.println("Unable to verify station. Please try again.");
                        continue;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Unable to read source station. Please try again.");
                    continue;
                }
            }
            
            String Dest;
            while (true) {
                System.out.print("Destination: ");
                try {
                    Dest = k.next();
                    if (!Validation.isValidStationName(Dest)) {
                        System.out.println("Invalid destination station. Please enter a valid station name (letters only).");
                        continue;
                    }
                    if (Dest.equalsIgnoreCase(sourcefrom)) {
                        System.out.println("Destination cannot be same as source station.");
                        continue;
                    }
                     // Check if station exists in database
                    try (Connection con = DBConnection.getConnection()) {
                        PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM train WHERE source = ? OR destination = ?");
                        ps.setString(1, Dest);
                        ps.setString(2, Dest);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            System.out.println("Station not found in our network. Please enter a valid station.");
                            continue;
                        }
                    } catch (Exception e) {
                        // If DB check fails, continue anyway
                    }
                    
                    // Check if destination station exists in database
                    try (Connection con = DBConnection.getConnection()) {
                        PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM train WHERE LOWER(source) = LOWER(?) OR LOWER(destination) = LOWER(?)");
                        ps.setString(1, Dest);
                        ps.setString(2, Dest);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            System.out.println("Destination station '" + Dest + "' not found in our railway network. Please enter a valid station.");
                            continue;
                        }
                    } catch (Exception e) {
                        System.out.println("Unable to verify station. Please try again.");
                        continue;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Unable to read destination station. Please try again.");
                    continue;
                }
            }
            
            String Datee;
            while (true) {
                System.out.print("Date (yyyy-MM-dd): ");
                try {
                    Datee = k.next();
                    if (!Datee.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        System.out.println("Invalid date format. Please use yyyy-MM-dd format .");
                        continue;
                    }
                    
                    // Basic date validation
                    try {
                        java.time.LocalDate searchDate = java.time.LocalDate.parse(Datee);
                        if (searchDate.isBefore(java.time.LocalDate.now())) {
                            System.out.println("Cannot search for past dates. Please enter a current or future date.");
                            continue;
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid date. Please enter a valid date in yyyy-MM-dd format.");
                        continue;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Unable to read travel date. Please try again.");
                    continue;
                }
            }
            
            FetchDetails(sourcefrom, Dest, Datee);
        } catch (Exception e) {
            System.out.println(" Train search is currently unavailable. Please try again later.");
        }
    }// Fetch train details
    public static void FetchDetails(String sourcefrom, String Dest, String Datee) {
        try(Connection con = DBConnection.getConnection()) {
            if (con == null) {
                System.out.println(" Unable to connect to railway database. Please try again later.");
                return;
            } 
            String sql = "SELECT train_no,name,source,destination,departure_time,arrival_time FROM Train WHERE source=? AND destination=? AND DATE(departure_time)=? And status = 'Running'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sourcefrom);
            ps.setString(2, Dest);
            ps.setString(3, Datee);
            
            ResultSet r = ps.executeQuery();
           System.out.printf("%-10s %-25s %-15s %-15s %-20s %-20s%n","Train No", "Name", "Source", "Destination", "Departure", "Arrival" );
            System.out.println("--------------------------------------------------------------------------------------------------------------");
            boolean now = false;
            try {
                while(r.next()){
                    now = true;
                    System.out.printf("%-10s %-25s %-15s %-15s %-20s %-20s%n",
                            r.getInt("train_no"), r.getString("name"), r.getString("source"),
                            r.getString("destination"), r.getString("departure_time"), r.getString("arrival_time") );
                }
            } catch (Exception e) {
                System.out.println(" Unable to display train information. Please try again.");
            }
            if(!now) System.out.println("Trains Not Available at this Route..");
            else {
                System.out.println("\nPress Enter to return to dashboard...");
                try {
                    k.nextLine();
                } catch (Exception e) {
                    System.out.println(" Press Enter to continue...");
                }
            }
        } catch(java.sql.SQLException e){
            System.out.println(" enter valid details and Please try again.");
        } catch(Exception e){
            System.out.println(" Train search failed. Please try again later.");
        }
    }}
