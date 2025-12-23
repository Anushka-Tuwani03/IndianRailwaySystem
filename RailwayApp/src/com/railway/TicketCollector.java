package com.railway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TicketCollector {
    private String name;

    public TicketCollector(String name) {
        this.name = name;
    }

    public void checkTicket(PassengerData p) {
        try {
            System.out.println("\n Checking PNR: " + p.passenger_id);
            if (!p.valid) {
                System.out.println(" Invalid Ticket! Applying fine...");
                applyFine(p);
                return;
            }

            switch (p.status.toLowerCase()) {
                case "confirmed":
                    System.out.println(" Seat Confirmed: " + p.seatNo);
                    showPassengerDetails(p);
                    break;
                case "rac":
                    System.out.println(" Ticket is RAC (Partial seat).");
                    showPassengerDetails(p);
                    break;
                case "wl":
                    System.out.println(" Ticket is WAITLIST.");
                    showPassengerDetails(p);
                    break;
                default:
                    System.out.println(" Invalid Status Found.");
                    break;
            }
        } catch (Exception e) {
            System.out.println(" Unable to check ticket. Please try again later.");
        }
    }
    public void applyFine(PassengerData p) {
        try {
            System.out.println(" Fine of Rs. 500 charged to passenger: " + p.name);
            try (Connection con = DBConnection.getConnection()) {
                addFineToDB(con, p);
            } catch (SQLException e) {
                System.out.println(" Unable to record fine in database. Please try again later.");
            } catch (Exception e) {
                System.out.println(" Fine processing failed. Please try again later.");
            }
        } catch (Exception e) {
            System.out.println(" Unable to apply fine. Please try again later.");
        }
    }

    public void upgradePassenger(PassengerData p) {
        try {
            System.out.println("\n Upgrading Passenger: " + p.name);
            String oldStatus = p.status;
            if (oldStatus.equalsIgnoreCase("RAC")) {
                p.status = "Confirmed";
                p.seatNo = "B2-40"; 
                System.out.println(" RAC upgraded to CONFIRMED for Passenger_id " + p.passenger_id);
            } else if (oldStatus.equalsIgnoreCase("WL")) {
                p.status = "RAC";
                System.out.println(" WL upgraded to RAC for Passenger_id " + p.passenger_id);
            } else {
                System.out.println("  Already Confirmed. No upgrade needed.");
            }

            try (Connection con = DBConnection.getConnection()) {
                updatePassengerStatusInDB(con, p);
            } catch (SQLException e) {
                System.out.println(" Unable to update passenger status in database. Please try again later.");
            } catch (Exception e) {
                System.out.println(" Upgrade processing failed. Please try again later.");
            }
        } catch (Exception e) {
            System.out.println(" Unable to upgrade passenger. Please try again later.");
        }
    }

    public void reportMisuse(PassengerData p, String reason) {
        try {
            System.out.println(" Misuse Report: " + reason + " for Passenger " + p.name + " (Passenger_id " + p.passenger_id + ")");
            try (Connection con = DBConnection.getConnection()) {
                addMisuseReportToDB(con, p, reason);
            } catch (SQLException e) {
                System.out.println(" Unable to record misuse report in database. Please try again later.");
            } catch (Exception e) {
                System.out.println(" Misuse reporting failed. Please try again later.");
            }
        } catch (Exception e) {
            System.out.println(" Unable to report misuse. Please try again later.");
        }
    
    }
    private void addFineToDB(Connection con, PassengerData p) throws SQLException {
        String query = "INSERT INTO fine (pnr, reason, amount, fine_date) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, p.passenger_id);
            ps.setString(2, "Invalid Ticket");
            ps.setDouble(3, 500.0);
            ps.executeUpdate();
            System.out.println("Fine entry added to the database.");
        }
    }

    private void addMisuseReportToDB(Connection con, PassengerData p, String reason) throws SQLException {
        String query = "INSERT INTO misuse_report (pnr, report_type, description, report_date) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, p.passenger_id);
            ps.setString(2, reason);
            ps.setString(3, "Reported by TC: " + this.name);
            ps.executeUpdate();
            System.out.println("Misuse report added to the database.");
        }
    }

    private void updatePassengerStatusInDB(Connection con, PassengerData p) throws SQLException {
        String query = "UPDATE booking SET status = ? WHERE pnr = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, p.status);
            ps.setString(2, p.passenger_id);
            ps.executeUpdate();
            System.out.println("Passenger status updated in the database.");
        }
    }

    public void showPassengerDetails(PassengerData p) {
        try {
            System.out.println("\n --- Passenger Details ---");
            System.out.println("PNR: " + p.passenger_id);
            System.out.println("Name: " + p.name);
            System.out.println("Age: " + p.age);
            System.out.println("Gender: " + p.gender);
            System.out.println("ID Proof: " + p.id_proof);
            System.out.println("Email: " + p.email);
            System.out.println("Seat No: " + p.seatNo);
            System.out.println("Status: " + p.status);
            System.out.println("Valid Ticket: " + p.valid);
        } catch (Exception e) {
            System.out.println(" Unable to display passenger details. Please try again later.");
        }
    }
}