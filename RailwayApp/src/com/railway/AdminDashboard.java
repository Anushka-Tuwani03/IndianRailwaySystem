package com.railway;
import java.util.Scanner;

public class AdminDashboard {
    public static Scanner sc = new Scanner(System.in);
    public static void startAdmin(int adminUserId) {
    try {
     while (true) {
     try {
     System.out.println("\n<=================== Admin Dashboard ===================>");
     System.out.println("Welcome, Admin!");
     System.out.println(" 1. Staff Management");
        System.out.println(" 2. Revenue & Complaints");
         System.out.println(" 3. Dynamic Pricing Management"); 
         System.out.println(" 4. Exit");
         System.out.print("Enter option: ");
        int choice;
        try {
        choice = sc.nextInt();
        sc.nextLine();
         if (choice < 1 || choice > 4) {
         System.out.println("Invalid choice! Please select a number between 1-4.");
        continue;
       }
        } catch (java.util.InputMismatchException e) {
         System.out.println("Please enter a valid number (1-4).");
         sc.nextLine();
        continue;
      }
     switch (choice) {
     case 1:
     try {
    AdminStaffManagement.staffMenu();
     } catch (Exception e) {
     System.out.println(" Staff management service unavailable. Please try again later.");
      }
      break;
     case 2:
      try {
      AdminReportsComplaints.reportsAndComplaintsMenu();
     } catch (Exception e) {
  System.out.println(" Reports and complaints service unavailable. Please try again later.");
   }
   break;
    case 3:
    try {
     DynamicPricing.adminPricingMenu();
     } catch (Exception e) {
      System.out.println(" Dynamic pricing service unavailable. Please try again later.");
       }
     break;
     case 4:
    System.out.println(" Exiting Admin Dashboard...");
     return;
    default:
   System.out.println("Invalid choice! Please select 1, 2, 3, or 4.");
     }
    } catch (Exception e) {
    System.out.println(" Dashboard error occurred. Please try again.");
   }
  }
 } catch (Exception e) {
   System.out.println(" Admin dashboard is currently unavailable. Please try again later.");
      }
    }
}
