package com.railway;

import java.sql.*;

public class RailwaySystem {

  public static void RailwayStaffRole(){
        try {
            System.out.println("~~~ Railway Management System ~~~");
      while (true) {
            try {
          showMenu();
         int choice = -1;
           while (choice == -1) {
              System.out.print("Enter your choice: ");
              try {
                int input = InputValidator.getValidInt("");
                  if (input >= 1 && input <= 8) {
                   choice = input;
             } else {
            System.out.println("Please enter a valid number (1-8).");
          }
           } catch (Exception e) {
              System.out.println("Please enter a valid number (1-8).");
                        }
                    }
       switch (choice) {
       case 1 : {
                try {
             addTrain();
          } catch (Exception e) {
              System.out.println("Unable to add train. Please try again later.");
               }
                }
                break;
      case 2 : {
                 try {
                                addCoaches();
                            } catch (Exception e) {
                                System.out.println("Unable to add coaches. Please try again later.");
                            }
                        }
                        break;
          case 3 : {
                try {
                 addPricing();
              } catch (Exception e) {
                     System.out.println("Unable to add pricing. Please try again later.");
                            }
                        }
                        break;
            case 4 : {
                   try {
                      allocateQuota();
                   } catch (Exception e) {
                    System.out.println("Unable to allocate quota. Please try again later.");
                            }
                        }
                        break;
          case 5 : {
                  try {
                     updateLiveStatus();
                  } catch (Exception e) {
                  System.out.println("Unable to update live status. Please try again later.");
                     }
                        }
                        break;
            case 6 : {
                 try {
                      viewTrains();
                     } catch (Exception e) {
                  System.out.println("Unable to view trains. Please try again later.");
                     }
                        }
            break;
             case 7 : {
                  try {
             viewTrainDetails();
              } catch (Exception e) {
      System.out.println("Unable to view train details. Please try again later.");
           }
          }
          break;
          case 8 : {
           System.out.println("Thank you for using Indian Railways Management System!");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            return;
             }
          default : System.out.println("Invalid choice! Please select 1-8.");
    }
                } catch (Exception e) {
                    System.out.println("System error occurred. Please try again.");
                }
               
            }
        } catch (Exception e) {
            System.out.println("Railway management system is currently unavailable. Please try again later.");
        }
    }
    
    static void showMenu() {
        System.out.println("\n~~~MENU ~~~");
        System.out.println("1. Add Train");
        System.out.println("2. Add Coaches");
        System.out.println("3. Add Pricing for Coaches");
        System.out.println("4. Allocate Quota");
        System.out.println("5. Update Live Status");
        System.out.println("6. View All Trains");
        System.out.println("7. View Train Details");
        System.out.println("8. Exit");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~`");
    }
    
    static void addTrain() {
        try {
            Train train = new Train();
            System.out.println("\n~~~ Add Train ~~~");
            
            while (true) {
                train.trainNo = InputValidator.getValidString("Enter train number (5 digits): ");
                if (!Validation.isValidTrainNumber(train.trainNo)) {
                    System.out.println("Train number must be exactly 5 digits (e.g., 12345).");
                    continue;
                }
                
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Train WHERE train_no = ?")) {
                    ps.setString(1, train.trainNo);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Train number already exists! Please use a different number.");
                        continue;
                    }
                } catch (SQLException e) {
                    System.out.println("Unable to verify train number. Please try again.");
                    continue;
                }
                break;
            }
            
            while (true) {
                train.name = InputValidator.getValidString("Enter train name: ");
                if (!Validation.isValidTrainName(train.name)) {
                    System.out.println("Train name must be at least 3 characters with letters (e.g., 'Rajdhani Express').");
                    continue;
                }
                break;
            }
            
            while (true) {
                train.source = InputValidator.getValidString("Enter source station: ");
                if (!Validation.isValidStationName(train.source)) {
                    System.out.println("Source station must contain only letters and spaces (e.g., 'New Delhi').");
                    continue;
                }
                break;
            }
            
            while (true) {
                train.destination = InputValidator.getValidString("Enter destination station: ");
                if (!Validation.isValidStationName(train.destination)) {
                    System.out.println("Destination station must contain only letters and spaces (e.g., 'Mumbai Central').");
                    continue;
                }
                if (train.destination.equalsIgnoreCase(train.source)) {
                    System.out.println("Destination cannot be same as source station.");
                    continue;
                }
                break;
            }
            
            while (true) {
                train.departureTime = InputValidator.getValidString("Enter departure time (yyyy-MM-dd HH:mm:ss): ");
                if (!Validation.isValidDateTime(train.departureTime)) {
                    System.out.println("Invalid datetime format! Use yyyy-MM-dd HH:mm:ss (e.g., 2024-01-15 14:30:00).");
                    continue;
                }
                break;
            }
            
            while (true) {
                train.arrivalTime = InputValidator.getValidString("Enter arrival time (yyyy-MM-dd HH:mm:ss): ");
                if (!Validation.isValidDateTime(train.arrivalTime)) {
                    System.out.println("Invalid datetime format! Use yyyy-MM-dd HH:mm:ss (e.g., 2024-01-15 18:45:00).");
                    continue;
                }
                break;
            }
            
            train.addTrain();
        } catch (Exception e) {
            System.out.println("Unable to add train. Please try again later.");
        }
    }
    
    static void addCoaches() {
        try {
            int trainId = selectTrain();
            if (trainId == -1) return;
            
            System.out.println("\n--- Add Coaches ---");
            while (true) {
                try {
                    Coach coach = new Coach();
                    coach.trainId = trainId;
                    
                    while (true) {
                        coach.classType = InputValidator.getValidString("Enter coach class (AC/Sleeper/General): ");
                        if (coach.classType.equalsIgnoreCase("AC") || 
                            coach.classType.equalsIgnoreCase("Sleeper") || 
                            coach.classType.equalsIgnoreCase("General")) {
                            break;
                        } else {
                            System.out.println(" Invalid class type! Please enter AC, Sleeper, or General.");
                        }
                    }
                    
                    while (true) {
                        String seatInput = InputValidator.getValidString("Enter total seats (1-30): ");
                        if (!Validation.isValidCoachCount(seatInput)) {
                            System.out.println(" Invalid seat count! Please enter a number between 1-30.");
                            continue;
                        }
                        coach.totalSeats = Integer.parseInt(seatInput);
                        break;
                    }
                    
                    coach.addCoach();
                    String addMore;
                    while (true) {
                        addMore = getValidString("Add another coach? (y/n): ");
                        if (addMore.toLowerCase().matches("^[yn]$")) {
                            break;
                        } else {
                            System.out.println(" Please enter 'y' for yes or 'n' for no.");
                        }
                    }
                    if (!addMore.toLowerCase().startsWith("y")) break;
                } catch (Exception e) {
                    System.out.println(" Unable to add coach. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println(" Coach addition service unavailable. Please try again later.");
        }
    }

    static void addPricing() {
        try {
            int trainId = selectTrain();
            if (trainId == -1) return;
            System.out.println("\n~~~ Add Pricing ~~~");
            while (true) {
                try {
                    Pricing price = new Pricing();
                    price.trainId = trainId;
                    while (true) {
                        price.classType = InputValidator.getValidString("Enter class type (AC/Sleeper/General): ");
                        if (price.classType.equalsIgnoreCase("AC") || 
                            price.classType.equalsIgnoreCase("Sleeper") || 
                            price.classType.equalsIgnoreCase("General")) {
                            break;
                        } else {
                            System.out.println(" Invalid class type! Please enter AC, Sleeper, or General.");
                        }
                    }
                    
                    while (true) {
                        String fareInput = InputValidator.getValidString("Enter base fare (₹10-₹50000): ");
                        if (!Validation.isValidFare(fareInput)) {
                            System.out.println(" Invalid fare! Please enter a valid amount between ₹10-₹50000.");
                            continue;
                        }
                        price.baseFare = Double.parseDouble(fareInput);
                        break;
                    }
                    
                    price.addPricing();
                    String addMore;
                    while (true) {
                        addMore = getValidString("Add pricing for another class? (y/n): ");
                        if (addMore.toLowerCase().matches("^[yn]$")) {
                            break;
                        } else {
                            System.out.println(" Please enter 'y' for yes or 'n' for no.");
                        }
                    }
                    if (!addMore.toLowerCase().startsWith("y")) break;
                } catch (Exception e) {
                    System.out.println(" Unable to add pricing. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println(" Pricing service unavailable. Please try again later.");
        }
    }
    
    static void allocateQuota() {
        try {
            int trainId = selectTrain();
            if (trainId == -1) return;
            
            Quota quota = new Quota();
            System.out.println("\n--- Allocate Quota ---");
            quota.trainId = trainId;
            
            while (true) {
                String input = InputValidator.getValidString("Enter general seats (1-50): ");
                try {
                    quota.generalSeats = Integer.parseInt(input);
                    if (quota.generalSeats < 1 || quota.generalSeats > 500) {
                        System.out.println(" General seats must be between 1-30.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println(" Please enter a valid number.");
                }
            }
            
            while (true) {
                String input = InputValidator.getValidString("Enter tatkal seats (1-10): ");
                try {
                    quota.tatkalSeats = Integer.parseInt(input);
                    if (quota.tatkalSeats < 1 || quota.tatkalSeats > 100) {
                        System.out.println(" Tatkal seats must be between 1-30.");
                        continue;
                    }
                    if (quota.tatkalSeats > quota.generalSeats) {
                        System.out.println(" Tatkal seats cannot exceed general seats.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println(" Please enter a valid number.");
                }
            }
            
            while (true) {
                String input = InputValidator.getValidString("Enter ladies seats (1-30): ");
                try {
                    quota.ladiesSeats = Integer.parseInt(input);
                    if (quota.ladiesSeats < 1 || quota.ladiesSeats > 50) {
                        System.out.println(" Ladies seats must be between 1-30.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println(" Please enter a valid number.");
                }
            }
            
            while (true) {
                String input = InputValidator.getValidString("Enter senior citizen seats (1-30): ");
                try {
                    quota.seniorCitizenSeats = Integer.parseInt(input);
                    if (quota.seniorCitizenSeats < 1 || quota.seniorCitizenSeats > 30) {
                        System.out.println(" Senior citizen seats must be between 1-30.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println(" Please enter a valid number.");
                }
            }
            
            quota.allocateQuota();
        } catch (Exception e) {
            System.out.println(" Unable to allocate quota. Please try again later.");
        }
    }
    
    static void updateLiveStatus() {
        try {
            int trainId = selectTrain();
            if (trainId == -1) return;
            
            LiveStatus status = new LiveStatus();
            System.out.println("\n--- Update Live Status ---");
            status.trainId = trainId;
            
            while (true) {
                status.delayInfo = InputValidator.getValidString("Enter delay info (e.g., 'On Time', '15 min delay', 'Cancelled'): ");
                if (status.delayInfo.trim().length() < 3) {
                    System.out.println(" Delay info must be at least 3 characters (e.g., 'On Time', '30 min delay').");
                    continue;
                }
                if (!status.delayInfo.matches(".*[a-zA-Z]+.*")) {
                    System.out.println(" Delay info must contain meaningful text.");
                    continue;
                }
                break;
            }
            
            while (true) {
                String input = InputValidator.getValidString("Enter platform number (1-20): ");
                try {
                    status.platformNo = Integer.parseInt(input);
                    if (status.platformNo < 1 || status.platformNo > 20) {
                        System.out.println(" Platform number must be between 1-20.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println(" Please enter a valid platform number.");
                }
            }
            
            while (true) {
                String input = InputValidator.getValidString("Is train cancelled? (yes/no or true/false): ").toLowerCase();
                if (input.matches("^(yes|y|true|t)$")) {
                    status.cancellationFlag = true;
                    break;
                } else if (input.matches("^(no|n|false|f)$")) {
                    status.cancellationFlag = false;
                    break;
                } else {
                    System.out.println(" Please enter yes/no or true/false.");
                }
            }
            
            status.updateLiveStatus();
            System.out.println("\n--- Updated Live Status ---");
            LiveStatus.displayLiveStatus(trainId);
        } catch (Exception e) {
            System.out.println(" Unable to update live status. Please try again later.");
        }
    }
    
    static void viewTrains() {
        try {
            // FORMATTING IMPROVED: Better train list display
            System.out.println("\n<================ All Trains ================>");
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM Train ORDER BY train_id");
                 ResultSet rs = ps.executeQuery()) {
                
                // FORMATTING IMPROVED: Fixed column widths to prevent mixing
                System.out.printf("%-5s %-8s %-20s %-12s %-12s %-19s %-19s %-10s%n", 
                                "ID", "Number", "Name", "Source", "Dest", "Departure", "Arrival", "Status");
                System.out.println("=".repeat(120));
                
                boolean hasTrains = false;
                while (rs.next()) {
                    hasTrains = true;
                    String trainName = rs.getString("name");
                    if (trainName.length() > 22) {
                        trainName = trainName.substring(0, 22) + "...";
                    }
                    
                    // FORMATTING IMPROVED: Truncate long station names and format timestamps
                    String source = rs.getString("source");
                    String destination = rs.getString("destination");
                    if (source.length() > 11) source = source.substring(0, 11) + ".";
                    if (destination.length() > 11) destination = destination.substring(0, 11) + ".";
                    
                    Timestamp depTime = rs.getTimestamp("departure_time");
                    Timestamp arrTime = rs.getTimestamp("arrival_time");
                    String depStr = (depTime != null) ? depTime.toString().substring(0, 16) : "N/A";
                    String arrStr = (arrTime != null) ? arrTime.toString().substring(0, 16) : "N/A";
                    
                    System.out.printf("%-5d %-8s %-20s %-12s %-12s %-19s %-19s %-10s%n",
                        rs.getInt("train_id"), 
                        rs.getString("train_no"), 
                        trainName,
                        source, 
                        destination,
                        depStr, 
                        arrStr, 
                        rs.getString("status"));
                }
                
                if (!hasTrains) {
                    System.out.println("\n❌ No trains found in the system.");
                    System.out.println("Add trains using option 1 to see them here.");
                } else {
                    System.out.println("=".repeat(120));
                    System.out.println("✅ Total trains displayed successfully!");
                }
                
            } catch (SQLException e) {
                System.out.println("❌ Unable to connect to train database. Please try again later.");
            } catch (Exception e) {
                System.out.println("❌ Error loading train information. Please try again later.");
            }
        } catch (Exception e) {
            System.out.println("❌ Train viewing service is currently unavailable. Please try again later.");
        }
    }
    
    static void viewTrainDetails() {
        int trainId = selectTrain();
        if (trainId == -1) return;
        
        // FORMATTING IMPROVED: Better train details display
        System.out.println("\n<================ Train Details ================>");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Coach WHERE train_id = ?")) {
            ps.setInt(1, trainId);
            ResultSet rs = ps.executeQuery();
            
            System.out.println("\n--- Coach Information ---");
            System.out.printf("%-10s %-15s %-10s %-10s%n", "Coach ID", "Class Type", "Total Seats", "Fare");
            System.out.println("-".repeat(50));
            
            boolean hasCoaches = false;
            while (rs.next()) {
                hasCoaches = true;
                System.out.printf("%-10d %-15s %-10d ₹%-9.2f%n",
                    rs.getInt("coach_id"), 
                    rs.getString("class_type"), 
                    rs.getInt("total_seats"),
                    rs.getDouble("fare"));
            }
            
            if (!hasCoaches) {
                System.out.println("❌ No coaches found for this train.");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error viewing coaches: " + e.getMessage());
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Quota WHERE train_id = ?")) {
            ps.setInt(1, trainId);
            ResultSet rs = ps.executeQuery();
            
            System.out.println("\n--- Quota Information ---");
            if (rs.next()) {
                System.out.printf("General Seats     : %d%n", rs.getInt("general_seats"));
                System.out.printf("Tatkal Seats      : %d%n", rs.getInt("tatkal_seats"));
                System.out.printf("Ladies Seats      : %d%n", rs.getInt("ladies_seats"));
                System.out.printf("Senior Citizen    : %d%n", rs.getInt("senior_citizen_seats"));
            } else {
                System.out.println("❌ No quota information found for this train.");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error viewing quota: " + e.getMessage());
        }
        
        System.out.println("===============================================");
    }
    
    static int selectTrain() {
        try {
            viewTrains();
            while (true) {
                String input = InputValidator.getValidString("Enter Train ID (or '0' to cancel): ");
                
                if (input.equals("0")) {
                    System.out.println("Operation cancelled.");
                    return -1;
                }
                
                int trainId;
                try {
                    trainId = Integer.parseInt(input);
                    if (trainId < 1) {
                        System.out.println("Train ID must be a positive number.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid Train ID number.");
                    continue;
                }
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Train WHERE train_id = ?")) {
                    ps.setInt(1, trainId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        return trainId;
                    } else {
                        System.out.println("Train ID not found! Please select a valid Train ID from the list above.");
                        continue;
                    }
                } catch (SQLException e) {
                    System.out.println("Unable to verify train ID. Please try again.");
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.println("Train selection service unavailable. Please try again later.");
            return -1;
        }
    }
    static int getValidInt(String p) {
        while (true) {
            System.out.print(p);
            try {
                return Integer.parseInt(InputValidator.getValidString("").trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please try again.");
            }
        }
    }
    static boolean getValidBoolean(String p) {
        while (true) {
            System.out.print(p);
            String input = InputValidator.getValidString("").trim().toLowerCase();
            if (input.equals("true") || input.equals("t") || input.equals("yes") || input.equals("y")) {
                return true;
            } else if (input.equals("false") || input.equals("f") || input.equals("no") || input.equals("n")) {
                return false;
            } else {
                System.out.println("Please enter true/false or y/n");
            }
        }
    }
    static String getValidString(String p) {
        System.out.print(p);
        return InputValidator.getValidString("");
    }
}