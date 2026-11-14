-- =============================================================
-- ParkWise - Intelligent Parking Allocation System
-- Database Schema
-- MySQL 8.x
-- =============================================================

CREATE DATABASE IF NOT EXISTS parkwise_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE parkwise_db;

-- -------------------------------------------------------------
-- Table: admins
-- Stores administrator accounts with salted password hashes.
-- Passwords are stored as SHA-256(salt + password), never plaintext.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admins (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,  -- format: salt:hash
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Table: vehicles
-- Stores registered vehicle information.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicles (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    registration_number VARCHAR(20)  NOT NULL UNIQUE,
    vehicle_type        ENUM('TWO_WHEELER','FOUR_WHEELER','ACCESSIBLE') NOT NULL,
    owner_name          VARCHAR(100) NOT NULL,
    contact_number      VARCHAR(15)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_registration (registration_number),
    INDEX idx_vehicle_type (vehicle_type)
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Table: parking_slots
-- Stores physical parking slot configuration.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parking_slots (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(10)  NOT NULL UNIQUE,
    slot_type   ENUM('TWO_WHEELER','FOUR_WHEELER','ACCESSIBLE') NOT NULL,
    slot_status ENUM('AVAILABLE','OCCUPIED','RESERVED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    INDEX idx_slot_type   (slot_type),
    INDEX idx_slot_status (slot_status),
    INDEX idx_active      (active)
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Table: tariffs
-- Configurable parking fee structure per vehicle type.
-- Uses DECIMAL to avoid floating-point monetary errors.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tariffs (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_type         ENUM('TWO_WHEELER','FOUR_WHEELER','ACCESSIBLE') NOT NULL UNIQUE,
    first_hour_fee       DECIMAL(10,2) NOT NULL,
    additional_hour_fee  DECIMAL(10,2) NOT NULL,
    active               TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Table: parking_sessions
-- Records every vehicle entry/exit pair.
-- One vehicle can have at most one ACTIVE session at a time.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parking_sessions (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id       INT           NOT NULL,
    slot_id          INT           NOT NULL,
    entry_time       DATETIME      NOT NULL,
    exit_time        DATETIME      NULL,
    duration_minutes INT           NULL,
    fee_amount       DECIMAL(10,2) NULL,
    payment_status   ENUM('PENDING','PAID','WAIVED') NOT NULL DEFAULT 'PENDING',
    session_status   ENUM('ACTIVE','COMPLETED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE RESTRICT,
    FOREIGN KEY (slot_id)    REFERENCES parking_slots(id) ON DELETE RESTRICT,
    INDEX idx_vehicle_session (vehicle_id, session_status),
    INDEX idx_slot_session    (slot_id, session_status),
    INDEX idx_entry_time      (entry_time),
    -- Prevent a vehicle from having two simultaneous ACTIVE sessions
    CONSTRAINT uq_vehicle_active_session UNIQUE (vehicle_id, session_status)
        -- This UNIQUE constraint only works when combined with application-level
        -- checks; MySQL does not support partial unique indexes on ENUM conditions.
        -- The constraint is enforced at application/transaction level (see DAO).
) ENGINE=InnoDB;

-- Drop the unconditional unique and replace with a proper application guard.
-- The actual uniqueness per ACTIVE session is enforced in the DAO transaction.
ALTER TABLE parking_sessions DROP INDEX uq_vehicle_active_session;

-- -------------------------------------------------------------
-- Table: reservations
-- Advance bookings with no-overlap constraint enforced by
-- the application transaction logic.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservations (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id          INT       NOT NULL,
    slot_id             INT       NOT NULL,
    start_time          DATETIME  NOT NULL,
    end_time            DATETIME  NOT NULL,
    reservation_status  ENUM('CONFIRMED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'CONFIRMED',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE RESTRICT,
    FOREIGN KEY (slot_id)    REFERENCES parking_slots(id) ON DELETE RESTRICT,
    INDEX idx_slot_reservation   (slot_id, reservation_status),
    INDEX idx_vehicle_reservation(vehicle_id, reservation_status),
    CONSTRAINT chk_reservation_times CHECK (end_time > start_time)
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Table: payments
-- Payment records linked to completed parking sessions.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    session_id     INT           NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH','CARD','UPI','WAIVED') NOT NULL DEFAULT 'CASH',
    payment_status ENUM('PENDING','COMPLETED','FAILED') NOT NULL DEFAULT 'COMPLETED',
    paid_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES parking_sessions(id) ON DELETE RESTRICT,
    INDEX idx_session_payment (session_id)
) ENGINE=InnoDB;
