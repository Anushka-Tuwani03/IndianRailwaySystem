package com.railway;

import java.sql.*;
import java.util.Scanner;

public class AdminReportsComplaints {
    private static Scanner sc = new Scanner(System.in);

    public static void reportsAndComplaintsMenu() {
        while(true) {
            try {
                System.out.println("\n ~~~ REVENUE & COMPLAINTS MANAGEMENT ~~~");
                System.out.println("1. Revenue Reports");
                System.out.println("2. View Complaints");
                System.out.println("3. Resolve Complaint");
                System.out.println("4. Back to Admin Menu");
                
                int ch = -1;
                while (ch == -1) {
                    System.out.print("Enter choice: ");
                    try {
                       int input = sc.nextInt();
                        sc.nextLine();
                        if (input >= 1 && input <= 4) {
                            ch = input;
                        } else {
                            System.out.println("Please enter a valid number (1-4).");
                        }
                    } catch (java.util.InputMismatchException e) {
                        System.out.println("Please enter a valid number (1-4).");
                        sc.nextLine();
                    }
                }

                switch(ch) {
                    case 1 -> {
                        try {
                            revenueReports();
                        } catch (Exception e) {
                            System.out.println(" Unable to generate revenue reports. Please try again later.");
                        }
                    }
                    case 2 -> {
                        try {
                            viewAllComplaints();
                        } catch (Exception e) {
                            System.out.println(" Unable to view complaints. Please try again later.");
                        }
                    }
                    case 3 -> {
                        try {
                            resolveComplaint();
                        } catch (Exception e) {
                            System.out.println(" Unable to resolve complaint. Please try again later.");
                        }
                    }
                    case 4 -> {
                        System.out.println("Returning to Admin Menu...");
                        return;
                    }
                    default -> System.out.println("Invalid choice! Please select 1-4.");
                }
            } catch (Exception e) {
                System.out.println("Reports and complaints service temporarily unavailable. Please try again later.");
            }
        }
    }
    private static void revenueReports() {
        while(true) {
            try {
                System.out.println("\n--- Revenue Report Period ---");
                System.out.println("1. Daily");
                System.out.println("2. Weekly");
                System.out.println("3. Monthly");
                System.out.println("4. Back");
                
               int ch = -1;
                while (ch == -1) {
                    System.out.print("Enter choice: ");
                    try {
                        int input = sc.nextInt();
                        sc.nextLine();
                        if (input >= 1 && input <= 4) {
                            ch = input;
                        } else {
                            System.out.println("Please enter a valid number (1-4).");
                        }
                    } catch (java.util.InputMismatchException e) {
                        System.out.println("Please enter a valid number (1-4).");
                        sc.nextLine();
                    }
                }
                String condition = "";
                String period = "";
                switch(ch) {
                    case 1 -> {
                        condition = "DATE(booking_date) = CURDATE()";
                        period = "Today's";
                    }
                    case 2 -> {
                        condition = "YEARWEEK(booking_date,1) = YEARWEEK(CURDATE(),1)";
                        period = "This Week's";
                    }
                    case 3 -> {
                        condition = "MONTH(booking_date)=MONTH(CURDATE()) AND YEAR(booking_date)=YEAR(CURDATE())";
                        period = "This Month's";
                    }
                    case 4 -> {
                        System.out.println("Returning to reports menu...");
                        return;
                    }
                    default -> {
                        System.out.println(" Invalid choice! Please select 1-4.");
                        continue;
                    }
                }
                try(Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps = con.prepareStatement(
                            "SELECT COUNT(*) as total_tickets, COALESCE(SUM(fare), 0) as total_revenue FROM booking WHERE " + condition);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        System.out.println("\n" + period + " Revenue Report:");
                        System.out.println("Total Tickets Sold: " + rs.getInt("total_tickets"));
                        System.out.println("Total Revenue: Rs." + String.format("%.2f", rs.getDouble("total_revenue")));
                    } else {
                        System.out.println("No revenue data found for the selected period.");
                    }
                } catch(Exception e) {
                    System.out.println("Unable to fetch revenue data. Please try again later.");
                }
            } catch (Exception e) {
                System.out.println("Revenue report service temporarily unavailable. Please try again later.");
            }
        }
    }
    private static void viewAllComplaints() {
        try(Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT complaint_id, pnr, complaint_text, status, created_at FROM complaints ORDER BY created_at DESC");
            ResultSet rs = ps.executeQuery();
            System.out.println("\n--- All Complaints ---");
            System.out.printf("%-5s %-12s %-30s %-10s %-20s%n", "ID", "PNR", "Complaint", "Status", "Created At");
            System.out.println("-".repeat(80));   
            boolean hasComplaints = false;
            while(rs.next()) {
                hasComplaints = true;
                String complaintText = rs.getString("complaint_text");
                if (complaintText.length() > 27) {
                    complaintText = complaintText.substring(0, 27) + "...";
                }
                
                System.out.printf("%-5d %-12s %-30s %-10s %-20s%n",
                    rs.getInt("complaint_id"),
                    rs.getString("pnr"),
                    complaintText,
                    rs.getString("status"),
                    rs.getTimestamp("created_at"));
            }
            
            if (!hasComplaints) {
                System.out.println("No complaints found in the system.");
                //try kiya h return
                
            }
        } catch(Exception e) {
            System.out.println("Unable to load complaints. Please try again later.");
        }
    }

    private static void resolveComplaint() {
        try {
            viewAllComplaints();  
            int cid;
            while (true) {
                try {
                    System.out.print("Enter Complaint ID to resolve (or 0 to cancel): ");
                    cid = sc.nextInt();
                    sc.nextLine();
                    
                    if (cid == 0) {
                        System.out.println("Operation cancelled.");
                        return;
                    }                  
                    if (cid < 1) {
                        System.out.println("Complaint ID must be a positive number.");
                        continue;
                    }
                    break;
                } catch (java.util.InputMismatchException e) {
                    System.out.println("Please enter a valid Complaint ID number.");
                    sc.nextLine();
                }
            }
            try (Connection con = DBConnection.getConnection()) {
                // Check if complaint exists and is not already resolved
                PreparedStatement checkComplaint = con.prepareStatement(
                    "SELECT status FROM complaints WHERE complaint_id = ?");
                checkComplaint.setInt(1, cid);
                ResultSet checkRs = checkComplaint.executeQuery();
                if (!checkRs.next()) {
                    System.out.println("Complaint ID not found! Please select a valid Complaint ID.");
                    return;
                }
                String currentStatus = checkRs.getString("status");
                if (currentStatus.equalsIgnoreCase("Resolved")) {
                    System.out.println("This complaint is already resolved.");
                    return;
                }
            
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE complaints SET status = 'Resolved' WHERE complaint_id = ?");
                ps.setInt(1, cid);
                int rows = ps.executeUpdate(); 
                if(rows > 0) {
                    System.out.println(" Complaint resolved successfully!");
                } else {
                    System.out.println(" Failed to resolve complaint. Please try again.");
                }
            }
        } catch(Exception e) {
            System.out.println("Error resolving complaint: " + e.getMessage());
        }
    }
}
