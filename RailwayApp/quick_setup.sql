-- =====================================================
-- QUICK SETUP - DYNAMIC PRICING DATABASE CHANGES
-- =====================================================
-- Copy and paste these commands in MySQL to setup dynamic pricing

USE IndianRailways;

-- Change concession_type to support flexible values
ALTER TABLE booking MODIFY COLUMN concession_type VARCHAR(50) DEFAULT 'None';

-- Add dynamic_fare column to store calculated fares
ALTER TABLE booking ADD COLUMN dynamic_fare DECIMAL(10,2) DEFAULT 0.00;

-- Verify changes
DESCRIBE booking;