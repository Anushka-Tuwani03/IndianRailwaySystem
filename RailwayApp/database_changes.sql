-- =====================================================
-- DYNAMIC PRICING SYSTEM - DATABASE CHANGES
-- =====================================================
-- Execute these queries to update your database for dynamic pricing functionality

USE IndianRailways;

-- =====================================================
-- 1. UPDATE BOOKING TABLE - CONCESSION TYPE FIELD
-- =====================================================
-- CHANGE: Convert concession_type from ENUM to VARCHAR for flexibility
-- REASON: Support new concession types like "senior citizen", "army", "differently abled"

ALTER TABLE booking 
MODIFY COLUMN concession_type VARCHAR(50) DEFAULT 'None'
COMMENT 'Flexible concession types: None, senior citizen, army, differently abled';

-- =====================================================
-- 2. ADD DYNAMIC FARE COLUMN TO BOOKING TABLE
-- =====================================================
-- NEW ADDITION: Store calculated dynamic fare separately from base fare
-- REASON: Track both original fare and final calculated fare for analysis

ALTER TABLE booking 
ADD COLUMN dynamic_fare DECIMAL(10,2) DEFAULT 0.00
COMMENT 'Final fare after dynamic pricing calculations (occupancy + tatkal + demand + concessions)';

-- =====================================================
-- 3. VERIFY PRICING TABLE STRUCTURE
-- =====================================================
-- CHECK: Ensure pricing table has required columns for dynamic pricing
-- These columns should already exist, but verify:

-- DESCRIBE pricing;
-- Expected columns:
-- - pricing_id (auto_increment primary key)
-- - train_id (foreign key to train table)
-- - class_type (varchar)
-- - base_fare (decimal)
-- - tatkal_multiplier (decimal, default 1.50)
-- - demand_multiplier (decimal, default 1.00)

-- =====================================================
-- 4. VERIFY COACH TABLE STRUCTURE
-- =====================================================
-- CHECK: Ensure coach table has required columns for occupancy calculation
-- These columns should already exist:

-- DESCRIBE coach;
-- Expected columns:
-- - coach_id (auto_increment primary key)
-- - train_id (foreign key)
-- - class_type (enum: 'Sleeper','AC','General')
-- - total_seats (int)
-- - fare (decimal)

-- =====================================================
-- 5. VERIFY SEAT TABLE STRUCTURE
-- =====================================================
-- CHECK: Ensure seat table has status tracking for occupancy
-- These columns should already exist:

-- DESCRIBE seat;
-- Expected columns:
-- - seat_id (auto_increment primary key)
-- - coach_id (foreign key)
-- - seat_no (varchar)
-- - status (enum: 'Available','Booked','WL','RAC')
-- - pnr (varchar)

-- =====================================================
-- 6. SAMPLE DATA INSERTION (OPTIONAL)
-- =====================================================
-- Insert sample pricing data if needed for testing

-- INSERT INTO pricing (train_id, class_type, base_fare, tatkal_multiplier, demand_multiplier) 
-- VALUES 
-- (1, 'AC', 500.00, 1.5, 1.0),
-- (1, 'Sleeper', 250.00, 1.5, 1.0),
-- (1, 'General', 100.00, 1.5, 1.0);

-- =====================================================
-- 7. VERIFICATION QUERIES
-- =====================================================
-- Run these to verify changes were applied correctly:

-- Check booking table structure
-- DESCRIBE booking;

-- Check if dynamic_fare column exists
-- SHOW COLUMNS FROM booking LIKE 'dynamic_fare';

-- Check concession_type field type
-- SHOW COLUMNS FROM booking LIKE 'concession_type';

-- Test dynamic pricing data
-- SELECT pnr, fare, dynamic_fare, concession_type FROM booking LIMIT 5;

-- =====================================================
-- SUMMARY OF CHANGES:
-- =====================================================
-- 1. booking.concession_type: ENUM -> VARCHAR(50) (for flexibility)
-- 2. booking.dynamic_fare: NEW COLUMN (stores calculated fare)
-- 
-- IMPACT:
-- - Supports new concession types required by dynamic pricing
-- - Tracks both base fare and final calculated fare
-- - Enables pricing analysis and reporting
-- - Maintains backward compatibility
-- =====================================================