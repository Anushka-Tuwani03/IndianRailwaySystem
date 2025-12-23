package com.railway;

import java.util.regex.Pattern;

public class Validation {

   
public static boolean isValidName(String name) {
    if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("null")) {
        return false;
    }
    return name.trim().matches("^[A-Za-z]+(\\s[A-Za-z]+)?$");
}

    public static boolean isValidAge(int age) {
        return age > 0 && age <= 120;
    }

    // gender M, F or O
    public static boolean isValidGender(String gender) {
        return gender != null &&
                (gender.equalsIgnoreCase("M") ||
                 gender.equalsIgnoreCase("F") ||
                 gender.equalsIgnoreCase("O"));
    }
//new//
    // phone number  10 digits and start with 6-9
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[6-9]\\d{9}$");
    }

    // email proper format validation
    public static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email != null && Pattern.matches(regex, email);
    }

    // id proof validation: "aadhar" + 4 digits
    public static boolean isValidIdProof(String id) {
        if (id == null) return false;
        // Format: "aadhar" followed by exactly 4 digits (case insensitive)
        return id.matches("^(?i)aadhar\\d{4}$");
    }

   public static boolean isValidUsername(String username) {
    if (username == null || username.trim().isEmpty() || username.equalsIgnoreCase("null")) {
        System.out.println("Invalid details! Please enter proper username (FirstName [LastName]).");
        return false;
    }
    return username.trim().matches("^[A-Za-z]+(\\s[A-Za-z]+)?$");
}

    // password 6 characters one letter and one number
    public static boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");
    }

    // paytm password validation: 4-6 digits
    public static boolean isValidPaytmPassword(String password) {
        return password != null && password.matches("^\\d{4,6}$");
    }
    
    // PNR 10 digits
    public static boolean isValidPNR(String pnr) {
        return pnr != null && pnr.matches("^\\d{10}$");
    }
    
    // Misuse reason validation: meaningful text with minimum length
    public static boolean isValidMisuseReason(String reason) {
        if (reason == null || reason.trim().length() < 5) {
            return false;
        }
        
        // Check if it contains at least some meaningful words
        String[] commonReasons = {
            "ticket", "seat", "behavior", "conduct", "smoking", "drinking", "loud", "music", 
            "phone", "disturbing", "fighting", "argument", "rude", "inappropriate", "harassment",
            "littering", "damage", "property", "food", "smell", "hygiene", "overcrowding",
            "reservation", "berth", "compartment", "toilet", "cleanliness", "noise", "shouting"
        };
        
        String lowerReason = reason.toLowerCase();
        for (String word : commonReasons) {
            if (lowerReason.contains(word)) {
                return true;
            }
        }
        
        // If no common words found, check if it has meaningful structure (letters + spaces)
        return reason.matches(".*[a-zA-Z]{3,}.*") && reason.split("\\s+").length >= 2;
    }
    
    // Train number validation: exactly 5 digits
    public static boolean isValidTrainNumber(String trainNo) {
        return trainNo != null && trainNo.matches("^\\d{5}$");
    }
    
    // Train name validation: meaningful name with letters
    public static boolean isValidTrainName(String name) {
        return name != null && name.trim().length() >= 3 && name.matches(".*[a-zA-Z]{2,}.*");
    }
    
    // Station name validation: meaningful station name
    public static boolean isValidStationName(String station) {
        if (station == null || station.trim().length() < 3) {
            return false;
        }
        // Must contain only letters and spaces, minimum 3 characters
        if (!station.matches("^[a-zA-Z\\s]+$")) {
            return false;
        } 
        // Must have at least 3 consecutive letters (not just random characters)
        return station.matches(".*[a-zA-Z]{3,}.*");
    }
    // DateTime validation: yyyy-MM-dd HH:mm:ss format
    public static boolean isValidDateTime(String dateTime) {
        if (dateTime == null) return false;
        return dateTime.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
    }  // Fare validation: positive number
    public static boolean isValidFare(String fare) {
        try {
            double amount = Double.parseDouble(fare);
            return amount > 0 && amount <= 50000; // Reasonable fare limit
        } catch (NumberFormatException e) {
            return false;
        }
    }
     // Coach count validation: reasonable number
    public static boolean isValidCoachCount(String count) {
        try {
            int num = Integer.parseInt(count);
            return num > 0 && num <= 30; // Reasonable coach limit
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
