-- =============================================================
-- ParkWise - Sample Data
-- Run AFTER schema.sql
-- =============================================================

USE parkwise_db;

-- -------------------------------------------------------------
-- Default admin account
-- The admin account is created automatically on first application
-- startup by DatabaseInitializer.java using PasswordUtil.hashPassword().
-- Default username: admin  |  Default password: Admin@123
-- Change the password after first login.
-- Do NOT insert admin records manually — the hash must be generated
-- by PasswordUtil to match the verification logic.
-- -------------------------------------------------------------

-- -------------------------------------------------------------
-- Parking Slots
-- A-series: Two-wheelers   (10 slots)
-- B-series: Four-wheelers  (10 slots)
-- C-series: Accessible     (2 slots)
-- -------------------------------------------------------------
INSERT INTO parking_slots (slot_number, slot_type, slot_status, active) VALUES
('A01','TWO_WHEELER','AVAILABLE',1),
('A02','TWO_WHEELER','AVAILABLE',1),
('A03','TWO_WHEELER','AVAILABLE',1),
('A04','TWO_WHEELER','AVAILABLE',1),
('A05','TWO_WHEELER','AVAILABLE',1),
('A06','TWO_WHEELER','AVAILABLE',1),
('A07','TWO_WHEELER','AVAILABLE',1),
('A08','TWO_WHEELER','AVAILABLE',1),
('A09','TWO_WHEELER','AVAILABLE',1),
('A10','TWO_WHEELER','AVAILABLE',1),
('B01','FOUR_WHEELER','AVAILABLE',1),
('B02','FOUR_WHEELER','AVAILABLE',1),
('B03','FOUR_WHEELER','AVAILABLE',1),
('B04','FOUR_WHEELER','AVAILABLE',1),
('B05','FOUR_WHEELER','AVAILABLE',1),
('B06','FOUR_WHEELER','AVAILABLE',1),
('B07','FOUR_WHEELER','AVAILABLE',1),
('B08','FOUR_WHEELER','AVAILABLE',1),
('B09','FOUR_WHEELER','AVAILABLE',1),
('B10','FOUR_WHEELER','AVAILABLE',1),
('C01','ACCESSIBLE','AVAILABLE',1),
('C02','ACCESSIBLE','AVAILABLE',1);

-- -------------------------------------------------------------
-- Tariffs (development/demo rates)
-- Two-wheeler  : ₹20 first hour, ₹10 per additional started hour
-- Four-wheeler : ₹40 first hour, ₹20 per additional started hour
-- Accessible   : ₹20 first hour, ₹10 per additional started hour
-- -------------------------------------------------------------
INSERT INTO tariffs (vehicle_type, first_hour_fee, additional_hour_fee, active) VALUES
('TWO_WHEELER',  20.00, 10.00, 1),
('FOUR_WHEELER', 40.00, 20.00, 1),
('ACCESSIBLE',   20.00, 10.00, 1);

-- -------------------------------------------------------------
-- Sample Vehicles
-- -------------------------------------------------------------
INSERT INTO vehicles (registration_number, vehicle_type, owner_name, contact_number) VALUES
('KA01AB1234','TWO_WHEELER', 'Ravi Kumar',    '9876543210'),
('KA02CD5678','FOUR_WHEELER','Priya Sharma',  '9845123456'),
('KA03EF9012','FOUR_WHEELER','Arjun Patel',   '9731234567'),
('KA04GH3456','TWO_WHEELER', 'Meena Nair',    '9612345678'),
('KA05IJ7890','ACCESSIBLE',  'Suresh Babu',   '9543216789'),
('MH01KL2345','TWO_WHEELER', 'Rohit Singh',   '9988776655'),
('DL02MN6789','FOUR_WHEELER','Anita Joshi',   '9900112233'),
('TN07OP3456','TWO_WHEELER', 'Karthik Raj',   '9123456780'),
('GJ05QR7890','FOUR_WHEELER','Deepa Menon',   '9234567801'),
('RJ03ST1234','TWO_WHEELER', 'Vijay Reddy',   '9345678012'),
('MH12UV5678','FOUR_WHEELER','Sneha Kulkarni','9456780123'),
('KA09WX9012','ACCESSIBLE',  'Ramesh Iyer',   '9567801234'),
('UP16YZ3456','TWO_WHEELER', 'Pooja Verma',   '9678012345'),
('WB20AB7890','FOUR_WHEELER','Amit Bose',     '9780123456'),
('AP11CD1234','TWO_WHEELER', 'Lakshmi Devi',  '9801234567'),
('KL08EF5678','FOUR_WHEELER','Sanjay Nambiar','9812345678'),
('HR26GH9012','TWO_WHEELER', 'Neha Gupta',    '9823456789'),
('PB10IJ3456','FOUR_WHEELER','Harpreet Singh','9834567890'),
('MP09KL7890','TWO_WHEELER', 'Divya Tiwari',  '9845678901'),
('CG04MN2345','FOUR_WHEELER','Rahul Mishra',  '9856789012'),
('OD15OP6789','ACCESSIBLE',  'Sunita Patnaik','9867890123');
