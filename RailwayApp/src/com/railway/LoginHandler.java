package com.railway;

import java.util.Scanner;

public class LoginHandler {
    private static Scanner k = new Scanner(System.in);
    // Passenger login/signup
    public static void handlePassengerLogin() {
        while (true) {
            System.out.println("Choose way :");
            System.out.println(" 1. New user(Signup)");
            System.out.println(" 2. Existing user(Login)");
            System.out.println(" 0. Back to Main Menu");
            System.out.print(" Enter Choice :- ");
            int p = k.nextInt();
            k.nextLine(); 
            
            if(p == 0) {
                System.out.println("Returning to the main menu...");
                return;
            }

            if (p == 1) {
                handlePassengerSignup();
                break;
            } else if (p == 2) {
                handlePassengerLoginProcess();
                break;
            } else {
                System.out.println("Enter valid option (1 or 2).");
            }
        }
    }
    
    // Passenger signup process
    private static void handlePassengerSignup() {
        System.out.println();
        System.out.println("Enter Valid Details");

        // Full Name
        String name;
        while (true) {
            System.out.print(" Full Name       :-  ");
            name = k.nextLine();
            if (Validation.isValidName(name)) break;
            System.out.println("Invalid Name. Enter full name (first and last).");
        }

        // Age
        int age;
        while (true) {
            System.out.print(" Age             :-  ");
            String ageInput = k.nextLine();
            try {
                age = Integer.parseInt(ageInput);
                if (Validation.isValidAge(age)) break;
                System.out.println("Invalid Age. Enter between 1 and 120.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a valid number.");
            }
        }

        // Gender
        String gender;
        while (true) {
            System.out.print(" Gender (M/F/O)  :-  ");
            gender = k.nextLine();
            if (Validation.isValidGender(gender)) break;
            System.out.println("Invalid Gender. Enter M / F / O.");
        }

        // ID Proof
        String Id;
        while (true) {
            System.out.print(" IdProof (aadhar + 4digits)        :-  ");
            Id = k.nextLine();
            if (Validation.isValidIdProof(Id)){
                if(!PassengerDB.isIdProofExists(Id)) break;
                System.out.println("Id proof already exists!! Please use different Id.");
            } else {
                System.out.println("Invalid ID Proof. Enter format : aadhar1234 ( (aadhar + 4digits) )");
            }
        }

        // Phone
        String number;
        while (true) {
            System.out.print(" Phone no.       :-  ");
            number = k.nextLine();
            if (Validation.isValidPhone(number)) {
                if (!PassengerDB.isPhoneExists(number)) break;
                System.out.println("Phone number already exists! Please use different number.");
            } else {
                System.out.println("Invalid Phone. Enter 10 digit number starting with 6-9.");
            }
        }

        // Email
        String email;
        while (true) {
            System.out.print(" EmailId         :-  ");
            email = k.nextLine();
            if (Validation.isValidEmail(email)) {
                if (!PassengerDB.isEmailExists(email)) break;
                System.out.println("Email already exists! Please use different email.");
            } else {
                System.out.println("Invalid Email format. Use format: example@domain.com");
            } 
        }

        // Username
        System.out.println("Create Username And Password ");
        String username;
        while (true) {
            System.out.print(" Username        :-  ");
            username = k.nextLine();
            if (Validation.isValidUsername(username)) {
                if (!PassengerDB.isUsernameExists(username)) break;
                System.out.println("Username already exists! Please choose different username.");
            } else {
                System.out.println("Invalid Username. Enter firstname lastname format (e.g., john smith).");
            }
        }

        // Password
        String password;
        while (true) {
            System.out.print(" Password        :-  ");
            password = k.nextLine();
            if (Validation.isValidPassword(password)) break;
            System.out.println("Invalid Password. Must contain at least 6 characters (letters + numbers).");
        }

        // Call PassengerDB.signup
        boolean success = PassengerDB.signup(name, age, gender, Id, number, email, username, password);
        if (success) {
            System.out.println("Signup Successful!");
            int userId = PassengerDB.login(username, password);
            Passenger.optionPassenger(userId);
        } else {
            System.out.println("Signup Failed! Try again.");
        }
    }
    
    // Passenger login process
    private static void handlePassengerLoginProcess() {
        System.out.println("Enter Username And Password To Login");

        int userId = -1;
        while (userId == -1) {
            System.out.print(" Username :- ");
            String user = k.nextLine().trim(); 
            
            if (user.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }
            System.out.print(" Password :- ");
            String password = k.nextLine();
            if (password.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }
            
            try {
                userId = PassengerDB.login(user, password);

                if (userId != -1) {
                    System.out.println(" Login Successful! Your User ID: " + userId);
                    try {
                        Passenger.optionPassenger(userId);
                    } catch (Exception e) {
                        System.out.println(" Dashboard service unavailable. Please try again later.");
                    }
                } else {
                    System.out.println(" Invalid username/password. Please try again.");
                }
            } catch (Exception e) {
                System.out.println(" Login service unavailable. Please try again later.");
            }
        }
    }
    
    // Generic login retry for staff roles
    public static void loginWithRetry(String role, int roleCode) {
        try {
           int userId = -1;
            while (userId == -1) {
                System.out.println("Enter Username and Password To Login (" + role + ") - Enter '0' to return");
                
                String username;
                while (true) {
                    System.out.print(" Username :- ");
                    username = k.nextLine();
                    if (username.equals("0")) {
                        System.out.println(" Returning to main menu...");
                        return;
                    }
                    if (username.trim().isEmpty()) {
                        System.out.println(" Username cannot be empty. Please enter a valid username.");
                        continue;
                    }
                    break;
                }
                
                String password;
                while (true) {
                    System.out.print(" Password :- ");
                    password = k.nextLine();
                    if (password.equals("0")) {
                        System.out.println(" Returning to main menu...");
                        return;
                    }
                    if (password.trim().isEmpty()) {
                        System.out.println(" Password cannot be empty. Please enter a valid password.");
                        continue;
                    }
                    break;
                }

                try {
                    switch (roleCode) {
                        case 2 -> userId = PassengerDB.loginTc(username, password);
                        case 3 -> userId = PassengerDB.loginRail(username, password);
                        case 4 -> userId = PassengerDB.loginAdmin(username, password);
                    }

                    if (userId != -1) {
                        System.out.println(" Login Successful! Your User ID: " + userId);
                    } else {
                        System.out.println(" Invalid username/password. Please try again.");
                    }
                } catch (Exception e) {
                    System.out.println(" Login service unavailable. Please try again later.");
                }
            }
        } catch (Exception e) {
            System.out.println(" " + role + " login service temporarily unavailable. Please try again later.");
        }
    }
}