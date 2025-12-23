package com.railway;

import java.sql.*;

public class PassengerDB {

    public static PassengerData getPassengerDataByPNR(String pnr) {
        PassengerData p = null;
        String query = "SELECT b.pnr, b.status, s.seat_no, p.name, p.age, p.gender, p.id_proof, p.email " +
               "FROM booking b " +
               "JOIN Passengerdetails p ON b.passenger_id = p.passenger_id " + // correct table name
               "LEFT JOIN seat s ON b.seat_id = s.seat_id " +
               "WHERE b.pnr = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, pnr.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new PassengerData(
                        rs.getString("pnr"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("id_proof"),
                        rs.getString("email"),
                        rs.getString("seat_no"),
                        rs.getString("status"),
                        true
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching passenger data by PNR: " + e.getMessage());
        }
       return p; 
    }

    public static String getTCNameById(int tcId) {
    String name = null;
    String query = "SELECT username FROM user WHERE user_id = ? AND role = 'Ticket Collector'";  

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(query)) {

        ps.setInt(1, tcId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                name = rs.getString("username"); 
                System.out.println(" name fetch "); // 'username' column se fetch karega
            }
        }

    } catch (SQLException e) {
        System.err.println("Error fetching TC name by ID: " + e.getMessage());
    }

    return name; 
}

public static void showAllPNRs() {
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT b.pnr, p.name, b.status FROM booking b JOIN Passengerdetails p ON b.passenger_id = p.passenger_id where status = 'Confirmed' ORDER BY b.pnr";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            System.out.println("\n=== Available PNRs ===");
            System.out.println("PNR\t\tName\t\tStatus");
            System.out.println("----------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-10s\t%-15s\t%s%n", 
                    rs.getString("pnr"), 
                    rs.getString("name"), 
                    rs.getString("status"));
            }
            System.out.println("----------------------------------------");
        } catch (SQLException e) {
            System.out.println("Unable to fetch PNR list. Please try again later.");
        }
    }


    public static boolean isPhoneExists(String phone) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM Passengerdetails WHERE phone = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, phone);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(" Unable to verify phone number. Please try again later.");
        } catch (Exception e) {
            System.out.println(" System error occurred. Please try again later.");
        }
        return false;
    }

    public static boolean isEmailExists(String email) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM Passengerdetails WHERE email = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(" Unable to verify email address. Please try again later.");
        } catch (Exception e) {
            System.out.println(" System error occurred. Please try again later.");
        }
        return false;
    }

    public static boolean isIdProofExists(String idProof) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM Passengerdetails WHERE id_proof = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, idProof);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(" Unable to verify ID proof. Please try again later.");
        } catch (Exception e) {
            System.out.println(" System error occurred. Please try again later.");
        }
        return false;
    }

    public static boolean isUsernameExists(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM User WHERE username = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(" Unable to verify username. Please try again later.");
        } catch (Exception e) {
            System.out.println(" System error occurred. Please try again later.");
        }
        return false;
    }
    public static boolean signup(String name, int age, String gender, String idProof,
                                 String phone, String email, String username, String password) {

     
        if (!Validation.isValidName(name) ||
            !Validation.isValidAge(age) ||
            !Validation.isValidGender(gender) ||
            !Validation.isValidIdProof(idProof) ||
            !Validation.isValidPhone(phone) ||
            !Validation.isValidEmail(email) ||
            !Validation.isValidUsername(username) ||
            !Validation.isValidPassword(password)) {
            System.out.println("Validation failed! Cannot signup.");
            return false;
        }

     
        if (isPhoneExists(phone)) {
            System.out.println("Phone number already exists!");
            return false;
        }
        if (isEmailExists(email)) {
            System.out.println("Email already exists!");
            return false;
        }
        if (isIdProofExists(idProof)) {
            System.out.println("ID Proof already exists!");
            return false;
        }
        if (isUsernameExists(username)) {
            System.out.println("Username already exists!");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // -------------------- Insert into User table --------------------
            String sqlUser = "INSERT INTO User(username, password, role) VALUES (?, ?, 'Passenger')";
            PreparedStatement pstUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            pstUser.setString(1, username);
            pstUser.setString(2, password);
            pstUser.executeUpdate();

            ResultSet rsUser = pstUser.getGeneratedKeys();
            int userId = 0;
            if (rsUser.next()) {
                userId = rsUser.getInt(1);
            }

            // -------------------- Insert into PassengerDetails table --------------------
            String sqlPass = "INSERT INTO Passengerdetails(user_id, name, age, gender, id_proof, phone, email) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstPass = conn.prepareStatement(sqlPass, Statement.RETURN_GENERATED_KEYS);
            pstPass.setInt(1, userId);
            pstPass.setString(2, name);
            pstPass.setInt(3, age);
            pstPass.setString(4, gender);
            pstPass.setString(5, idProof);
            pstPass.setString(6, phone);
            pstPass.setString(7, email);

            int rows = pstPass.executeUpdate();

            // get generated passenger_id
            int passengerId = 0;
            ResultSet rsPass = pstPass.getGeneratedKeys();
            if (rsPass.next()) {
                passengerId = rsPass.getInt(1);
            }

            //  Create Wallet entry 
            if (passengerId > 0) {
                String sqlWallet = "INSERT INTO Wallet(passenger_id, balance) VALUES (?, 0)";
                PreparedStatement pstWallet = conn.prepareStatement(sqlWallet);
                pstWallet.setInt(1, passengerId);
                pstWallet.executeUpdate();
            }

            return rows > 0;

        } catch (SQLException e) {
            System.out.println(" Registration failed due to database error. Please try again later.");
            return false;
        } catch (Exception e) {
            System.out.println(" Registration failed. Please try again later.");
            return false;
        }
    }

    //  LOGIN METHODS
    public static int login(String username, String password) {
        return loginByRole(username, password, "Passenger");
    }

    public static int loginTc(String username, String password) {
        return loginByRole(username, password, "TicketCollector");
    }

    public static int loginRail(String username, String password) {
        return loginByRole(username, password, "RailwayStaff");
    }

    public static int loginAdmin(String username, String password) {
        return loginByRole(username, password, "Admin");
    }

    private static int loginByRole(String username, String password, String role) {
        try (Connection conn = DBConnection.getConnection()) {
           String sql = "SELECT user_id FROM User WHERE TRIM(username)=? AND TRIM(password)=? AND TRIM(role)=?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username );
            pst.setString(2, password );
            pst.setString(3, role );

            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                int uid =  rs.getInt("user_id"); 
                return uid ;
            } else {
//System.out.println("Please enter valid details.....");
                return -1;
            }
        } catch (SQLException e) {
           System.out.println(" Login service temporarily unavailable. Please try again later.");
            return -1;
        } catch (Exception e) {
            System.out.println(" Login failed. Please try again later.");
            return -1;
        }
    }

    // GET PASSENGER ID BY USER_ID 
    public static int getPassengerIdByUserId(int userId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT passenger_id FROM Passengerdetails WHERE user_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("passenger_id");
            }
        } catch (SQLException e) {
            System.out.println(" Unable to access passenger information. Please try again later.");
        } catch (Exception e) {
            System.out.println(" System error occurred. Please try again later.");
        }
        return -1;
    }
}
