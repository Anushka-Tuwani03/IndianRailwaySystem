package com.railway;

import java.sql.*;
import java.util.Scanner;

public class AdminStaffManagement {
    private static Scanner sc = new Scanner(System.in);

    public static void staffMenu() {
        while (true) {
            try {
                System.out.println("\n~~~ STAFF MANAGEMENT ~~~");
                System.out.println("1. Add Staff");
                System.out.println("2. Remove Staff");
                System.out.println("3. View All Staff");
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

                switch (ch) {
                    case 1 -> {
                        try {
                            addStaff();
                        } catch (Exception e) {
                            System.out.println("Unable to add staff. Please try again later.");
                        }
                    }
                    case 2 -> {
                        try {
                            removeStaff();
                        } catch (Exception e) {
                            System.out.println("Unable to remove staff. Please try again later.");
                        }
                    }
                    case 3 -> {
                        try {
                            viewAllStaff();
                        } catch (Exception e) {
                            System.out.println("Unable to view staff. Please try again later.");
                        }
                    }
                    case 4 -> {
                        System.out.println("Returning to Admin Menu...");
                        return;
                    }
                    default -> System.out.println("Invalid choice! Please select 1-4.");
                }
            } catch (Exception e) {
                System.out.println("Staff management service temporarily unavailable. Please try again later.");
            }
        }
    }

    private static void addStaff() {
        try (Connection con = DBConnection.getConnection()) {
            // Staff Name Validation
            String name;
            while (true) {
                System.out.print("Enter staff name : ");
                name = sc.nextLine();
                if (!Validation.isValidName(name)) {
                    System.out.println("Invalid name! Please enter vaild name .");
                    continue;
                }
                break;
            }
             // Role Validation
            String role;
            while (true) {
                System.out.print("Enter role (RailwayStaff/TicketCollector): ");
                role = sc.nextLine();
                if (!role.equalsIgnoreCase("RailwayStaff") && !role.equalsIgnoreCase("TicketCollector")) {
                    System.out.println("Invalid role! Please enter 'RailwayStaff' or 'TicketCollector'.");
                    continue;
                }
                break;
            }
             // Phone Validation
            String phone;
            while (true) {
                System.out.print("Enter phone (10 digits): ");
                phone = sc.nextLine();
                if (!Validation.isValidPhone(phone)) {
                    System.out.println("Invalid phone! Please enter 10 digit number starting with 6-9.");
                    continue;
                }
                
                // Check if phone already exists
                PreparedStatement checkPhone = con.prepareStatement("SELECT COUNT(*) FROM staff WHERE phone = ?");
                checkPhone.setString(1, phone);
                ResultSet phoneRs = checkPhone.executeQuery();
                if (phoneRs.next() && phoneRs.getInt(1) > 0) {
                    System.out.println("Phone number already exists! Please use a different number.");
                    continue;
                }
                break;
            }

            String username = name.replaceAll("\\s","") + "_staff";
            String password = "pass123";
            
            // Check if username already exists
            PreparedStatement checkUser = con.prepareStatement("SELECT COUNT(*) FROM user WHERE username = ?");
            checkUser.setString(1, username);
            ResultSet userRs = checkUser.executeQuery();
            if (userRs.next() && userRs.getInt(1) > 0) {
                username = name.replaceAll("\\s","") + "_staff_" + System.currentTimeMillis() % 1000;
            }

            PreparedStatement psUser = con.prepareStatement(
                "INSERT INTO user(username,password,role) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
            psUser.setString(1, username);
            psUser.setString(2, password);
            psUser.setString(3, role);
            psUser.executeUpdate();
            ResultSet rs = psUser.getGeneratedKeys();
            int userId = 0;
            if(rs.next()) userId = rs.getInt(1);

            PreparedStatement psStaff = con.prepareStatement(
                "INSERT INTO staff(name,role,phone) VALUES (?,?,?)");
            psStaff.setString(1,name);
            psStaff.setString(2,role);
            psStaff.setString(3,phone);
            psStaff.executeUpdate();

            System.out.println("Staff added successfully!");
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
        } catch(Exception e) {
            System.out.println("Unable to add staff. Please try again later.");
        }
    }

    private static void removeStaff() {
        try(Connection con = DBConnection.getConnection()) {
            viewAllStaff();
            
            int staffId;
            while (true) {
                try {
                    System.out.print("Enter Staff ID to remove (or 0 to cancel): ");
                    staffId = sc.nextInt();
                    sc.nextLine();
                    
                    if (staffId == 0) {
                        System.out.println("Operation cancelled.");
                        return;
                    }
                    
                    if (staffId < 1) {
                        System.out.println("Staff ID must be a positive number.");
                        continue;
                    }
                    
                    // Check if staff exists
                    PreparedStatement checkStaff = con.prepareStatement("SELECT COUNT(*) FROM staff WHERE staff_id = ?");
                    checkStaff.setInt(1, staffId);
                    ResultSet checkRs = checkStaff.executeQuery();
                    if (checkRs.next() && checkRs.getInt(1) > 0) {
                        break;
                    } else {
                        System.out.println("Staff ID not found! Please select a valid Staff ID from the list above.");
                        continue;
                    }
                } catch (java.util.InputMismatchException e) {
                    System.out.println("Please enter a valid Staff ID number.");
                    sc.nextLine();
                }
            }
            
            // Confirmation
            String confirm;
            while (true) {
                System.out.print("Are you sure you want to remove this staff member? (yes/no): ");
                confirm = sc.nextLine().toLowerCase();
                if (confirm.equals("yes") || confirm.equals("y")) {
                    break;
                } else if (confirm.equals("no") || confirm.equals("n")) {
                    System.out.println("Operation cancelled.");
                    return;
                } else {
                    System.out.println("Please enter 'yes' or 'no'.");
                }
            }

            String getStaffQuery = "SELECT name, role FROM staff WHERE staff_id = ?";
            PreparedStatement psGetStaff = con.prepareStatement(getStaffQuery);
            psGetStaff.setInt(1, staffId);
            ResultSet staffRs = psGetStaff.executeQuery();
            
            String staffName = "";
            String staffRole = "";
            if (staffRs.next()) {
                staffName = staffRs.getString("name");
                staffRole = staffRs.getString("role");
            }
            
           
            PreparedStatement psStaff = con.prepareStatement("DELETE FROM staff WHERE staff_id=?");
            psStaff.setInt(1, staffId);
            int staffRows = psStaff.executeUpdate();
           
            if (staffRows > 0 && !staffName.isEmpty()) {
                String username = staffName.replaceAll("\\s", "") + "_staff";
                
               
                String deleteUserQuery = "DELETE FROM user WHERE username = ? AND role = ?";
                PreparedStatement psUser = con.prepareStatement(deleteUserQuery);
                psUser.setString(1, username);
                psUser.setString(2, staffRole);
                int userRows = psUser.executeUpdate();
               
                if (userRows == 0) {
                    String deleteUserPatternQuery = "DELETE FROM user WHERE username LIKE ? AND role = ?";
                    PreparedStatement psUserPattern = con.prepareStatement(deleteUserPatternQuery);
                    psUserPattern.setString(1, staffName.replaceAll("\\s", "") + "_staff%");
                    psUserPattern.setString(2, staffRole);
                    userRows = psUserPattern.executeUpdate();
                }
                
                if (userRows > 0) {
                    System.out.println("Staff and associated user account removed successfully!");
                } else {
                    System.out.println("Staff removed successfully! (User account may have been already deleted)");
                }
            } else {
                System.out.println("Failed to remove staff. Please try again.");
            }
        } catch(Exception e) {
            System.out.println("Unable to remove staff. Please try again later.");
        }
    }
 private static void viewAllStaff() {
        try(Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT staff_id,name,role,phone FROM staff ORDER BY staff_id");
            ResultSet rs = ps.executeQuery();
            
            System.out.println("\n<==================== All Staff Members ====================>");
            System.out.printf("%-5s %-25s %-20s %-15s%n", "ID", "Name", "Role", "Phone");
            System.out.println("=".repeat(70));
            
            boolean hasStaff = false;
            while(rs.next()) {
                hasStaff = true;
                String staffName = rs.getString("name");
                String staffRole = rs.getString("role");
                
                if (staffName.length() > 24) {
                    staffName = staffName.substring(0, 24) + ".";
                }
                if (staffRole.length() > 19) {
                    staffRole = staffRole.substring(0, 19) + ".";
                }
                
                System.out.printf("%-5d %-25s %-20s %-15s%n",
                        rs.getInt("staff_id"),
                        staffName,
                        staffRole,
                        rs.getString("phone"));
            }
            
            if (!hasStaff) {
                System.out.println("\n No staff members found in the system.");
                System.out.println("Add staff using option 1 to see them here.");
            } else {
                System.out.println("=".repeat(70));
                
            }
            
        } catch(Exception e) {
          System.out.println(" Error fetching staff: "+e.getMessage());
        }
    }
}
