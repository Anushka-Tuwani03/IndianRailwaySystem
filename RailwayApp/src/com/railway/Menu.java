package com.railway;

import java.util.Scanner;

public class Menu {
    public static Scanner k = new Scanner(System.in);
    public static void login(int a) {
        switch (a) {
            case 1:
                
                LoginHandler.handlePassengerLogin();
                break;

            case 2:
                TicketCollectorHandler.tcMainMenu();
                break;

            case 3:
                LoginHandler.loginWithRetry("Railway Staff", 3);
                RailwaySystem.RailwayStaffRole();
                break;

            case 4:
                LoginHandler.loginWithRetry("Admin", 4);
                int adminUserId = 5;
                AdminDashboard.startAdmin(adminUserId);
                break;

            case 5:
                System.out.println("Thank you for using Indian Railway System!");
                System.exit(0);
                break;

            default:
                System.out.println("Enter Valid option.");
                break;
        }
    }

    public static void main(String[] args) {
        System.out.println();
        System.out.println("<================================ Welcome To Indian Railway System ================================>");
        System.out.println();
        
        while (true) {
            System.out.println("Select Roles To Login/Signup");
            System.out.println(" 1. Passenger");
            System.out.println(" 2. Ticket Collector");
            System.out.println(" 3. Railway Staff");
            System.out.println(" 4. Admin");
            System.out.println(" 5. Exit");
            System.out.println();

            System.out.print("Choose Role :-  ");
            int c = k.nextInt();
            k.nextLine(); 

            login(c);
        }
    }
}