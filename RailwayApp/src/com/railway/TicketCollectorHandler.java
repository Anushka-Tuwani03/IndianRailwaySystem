package com.railway;

import java.util.Scanner;

public class TicketCollectorHandler {
    private static Scanner k = new Scanner(System.in);
    private static boolean returnToMainMenu = false;
    
    public static void tcMainMenu() {
        while (true) {
            try {
                if (returnToMainMenu) {
                    returnToMainMenu = false;
                    return;
                }
                System.out.println("\n<================ Ticket Collector Dashboard ================>");
                System.out.println(" 1. Login");
                System.out.println(" 0. Back to Main Menu");
                
                int choice;
                try {
                    System.out.print("Enter choice: ");
                    choice = k.nextInt();
                    k.nextLine();
                } catch (java.util.InputMismatchException e) {
                    System.out.println(" Please enter a valid number (0 or 1).");
                    k.nextLine();
                    continue;
                }
                
                switch (choice) {
                    case 1:
                        tcLogin();
                        if (returnToMainMenu) return;
                        break;
                    case 0:
                        System.out.println(" Returning to main menu...");
                        return;
                    default:
                        System.out.println(" Invalid choice! Please select 0 or 1.");
                }
            } catch (Exception e) {
                System.out.println(" TC dashboard temporarily unavailable. Please try again later.");
            }
        }
    }
    
    private static void tcLogin() {
        try {
            int tcId = -1;
            while (tcId == -1) {
                System.out.println("Enter Username and Password To Login (Ticket Collector) - Enter '0' to return");
                System.out.print(" Username :- ");
                String username = k.nextLine();
                if (username.equals("0")) {
                    System.out.println(" Returning to TC menu...");
                    return;
                }
                System.out.print(" Password :- ");
                String password = k.nextLine();
                if (password.equals("0")) {
                    System.out.println(" Returning to TC menu...");
                    return;
                }

                try {
                    tcId = PassengerDB.loginTc(username, password);

                    if (tcId != -1) {
                        System.out.println(" Login Successful! Your TC ID: " + tcId);
                        String tcName = PassengerDB.getTCNameById(tcId);
                        TicketCollector tc = new TicketCollector(tcName);
                        tcWorkingMenu(tc);
                        
                        return;

                    } else {
                        System.out.println(" Invalid username/password. Please try again.");
                    }
                } catch (Exception e) {
                    System.out.println(" Login service unavailable. Please try again later.");
                }
            }
        } catch (Exception e) {
            System.out.println(" TC login service temporarily unavailable. Please try again later.");
        }
    }

    private static void tcWorkingMenu(TicketCollector tc) {
        Scanner sc = new Scanner(System.in);
        
        while (true) {
            try {
                if (returnToMainMenu) {
                    returnToMainMenu = false;
                    return;
                }
                
                System.out.println("\n<================ TC Working Menu ================>");
                System.out.println("1. Check Ticket");
                System.out.println("2. Apply Fine");
                System.out.println("3. Upgrade Passenger");
                System.out.println("4. Report Misuse");
                System.out.println("5. Show Passenger Details");
                System.out.println("6. Logout");
                System.out.println("0. Back to Main Menu");
                
                int choice = -1;
                while (choice == -1) {
                    System.out.print("Enter choice: ");
                    try {
                        choice = sc.nextInt();
                        sc.nextLine();
                        if (choice < 0 || choice > 7) {
                            System.out.println(" Please enter a valid choice (0-7).");
                            choice = -1;
                        }
                    } catch (java.util.InputMismatchException e) {
                        System.out.println(" Please enter a valid number (0-7).");
                        sc.nextLine();
                    }
                }
                
                switch (choice) {
                    case 1 -> handleTicketCheck(tc);
                    case 2 -> handleFineApplication(tc);
                    case 3 -> handlePassengerUpgrade(tc);
                    case 4 -> handleMisuseReport(tc);
                    case 5 -> handlePassengerDetails(tc);
                    case 6 -> {
                        System.out.println(" Logging out...");
                        return;
                    }
                    case 0 -> {
                        System.out.println(" Returning to main menu...");
                        returnToMainMenu = true;
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println(" TC working menu temporarily unavailable. Please try again later.");
            }
        }
    }
    
    
    private static void handleTicketCheck(TicketCollector tc) {
        try {
            String pnr = getPNRInput("Enter PNR to check ticket: ");
            if (pnr == null) return;
            
            PassengerData pData = PassengerDB.getPassengerDataByPNR(pnr.trim());
            if (pData != null) {
                tc.checkTicket(pData);
            } else {
                System.out.println(" PNR not found!");
            }
        } catch (Exception e) {
            System.out.println(" Unable to check ticket. Please try again later.");
        }
    }
    
    private static void handleFineApplication(TicketCollector tc) {
        try {
            String pnr = getPNRInput("Enter PNR to apply fine: ");
            if (pnr == null) return;
            
            PassengerData pData = PassengerDB.getPassengerDataByPNR(pnr.trim());
            if (pData != null) {
                tc.applyFine(pData);
            } else {
                System.out.println(" PNR not found!");
            }
        } catch (Exception e) {
            System.out.println(" Unable to apply fine. Please try again later.");
        }
    }
    
    private static void handlePassengerUpgrade(TicketCollector tc) {
        try {
            String pnr = getPNRInput("Enter PNR to upgrade: ");
            if (pnr == null) return;
            
            PassengerData pData = PassengerDB.getPassengerDataByPNR(pnr.trim());
            if (pData != null) {
                tc.upgradePassenger(pData);
            } else {
                System.out.println(" PNR not found!");
            }
        } catch (Exception e) {
            System.out.println(" Unable to upgrade passenger. Please try again later.");
        }
    }
    
    private static void handleMisuseReport(TicketCollector tc) {
        try {
            String pnr = getPNRInput("Enter PNR to report misuse: ");
            if (pnr == null) return;
            
            PassengerData pData = PassengerDB.getPassengerDataByPNR(pnr.trim());
            if (pData != null) {
                String reason;
                while (true) {
                    System.out.print("Enter misuse reason: ");
                    reason = k.nextLine();
                    
                    if (reason.trim().isEmpty()) {
                        System.out.println(" Reason cannot be empty.");
                        continue;
                    }
                    
                    if (!Validation.isValidMisuseReason(reason)) {
                        System.out.println(" Please provide a meaningful reason (minimum 5 characters).");
                        continue;
                    }
                    break;
                }
                tc.reportMisuse(pData, reason);
            } else {
                System.out.println(" PNR not found!");
            }
        } catch (Exception e) {
            System.out.println(" Unable to report misuse. Please try again later.");
        }
    }
    
    private static void handlePassengerDetails(TicketCollector tc) {
        try {
            String pnr = getPNRInput("Enter PNR to show details: ");
            if (pnr == null) return;
            
            PassengerData pData = PassengerDB.getPassengerDataByPNR(pnr.trim());
            if (pData != null) {
                tc.showPassengerDetails(pData);
            } else {
                System.out.println(" PNR not found!");
            }
        } catch (Exception e) {
            System.out.println(" Unable to show passenger details. Please try again later.");
        }
    }
    
    private static String getPNRInput(String prompt) {
        PassengerDB.showAllPNRs();
        while (true) {
            System.out.print(prompt);
            String pnr = k.nextLine();
            
            if (!Validation.isValidPNR(pnr)) {
                System.out.println(" Invalid PNR format! PNR must be exactly 10 digits.");
                continue;
            }
            return pnr;
        }
    }
    
    private static void tcPassengerMenu(TicketCollector tc, PassengerData pData) {
        Scanner sc = new Scanner(System.in);
        
        while (true) {
            try {
                System.out.println("\n--- Ticket Collector Menu ---");
                System.out.println("1. Check Ticket");
                System.out.println("2. Apply Fine");
                System.out.println("3. Upgrade Passenger");
                System.out.println("4. Report Misuse");
                System.out.println("5. Show Passenger Details");
                System.out.println("6. Back to TC Menu");
                System.out.print("Enter choice: ");
                
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1 -> tc.checkTicket(pData);
                    case 2 -> tc.applyFine(pData);
                    case 3 -> tc.upgradePassenger(pData);
                    case 4 -> {
                        System.out.print("Enter misuse reason: ");
                        String reason = sc.nextLine();
                        tc.reportMisuse(pData, reason);
                    }
                    case 5 -> tc.showPassengerDetails(pData);
                    case 6 -> {
                        System.out.println("Returning to TC menu...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println(" TC passenger menu temporarily unavailable. Please try again later.");
                sc.nextLine();
            }
        }
    }
}