package com.railway;

import java.util.Scanner;

public class InputValidator {
    private static Scanner sc = new Scanner(System.in);
    
    public static int getValidInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Try again.");
            }
        }
    }
    
    public static double getValidDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Try again.");
            }
        }
    }
    
    public static String getValidString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
    
    public static String getValidNonEmptyString(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = sc.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Field required!");
            }
        } while (input.isEmpty());
        return input;
    }
    
    public static boolean getValidBoolean(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim().toLowerCase();
            if (input.equals("true") || input.equals("t") || input.equals("yes") || input.equals("y")) {
                return true;
            } else if (input.equals("false") || input.equals("f") || input.equals("no") || input.equals("n")) {
                return false;
            } else {
                System.out.println("Enter y/n");
            }
        }
    }
}