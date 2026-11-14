# ParkWise — Intelligent Parking Allocation System
## Comprehensive Project Report

---

**Project Title:** ParkWise — Intelligent Parking Allocation System  
**Technology:** Java 17, Java Swing, MySQL 8.x, JDBC, Maven  
**Project Type:** Desktop Application (CSE Academic Project)  
**Version:** 1.0.0  
**Report Date:** November 2026  

---

---

# TABLE OF CONTENTS

1. Introduction
2. Project Overview
3. Objectives
4. Scope and Limitations
5. System Requirements
6. Literature Review / Background
7. System Architecture
8. Database Design
9. Design Patterns Used
10. Module-wise Description
11. Class Descriptions
12. User Interface Design
13. Concurrency and Thread Safety
14. Security Implementation
15. Exception Handling
16. Testing
17. Fee Calculation Logic
18. Backup and Report Features
19. Data Flow Diagrams
20. Results and Screenshots Description
21. Future Enhancements
22. Conclusion
23. References
24. Appendix — Complete Source File Index

---

---

# CHAPTER 1: INTRODUCTION

## 1.1 Background

Urban parking management has long been a logistical challenge in densely populated cities and institutions. Manual parking management systems are inefficient, error-prone, and fail to provide real-time visibility into parking availability. As vehicular traffic increases, the demand for intelligent, automated parking allocation systems grows correspondingly.

ParkWise is a fully functional Java desktop application developed to address this need. It manages parking slots, vehicle entry and exit, automated fee calculation, advance reservations, revenue analytics, and administrative operations — all through a clean graphical user interface built with Java Swing.

## 1.2 Purpose of the Report

This document serves as a comprehensive technical and functional reference for the ParkWise system. It details the architecture, design decisions, implementation specifics, and testing methodology employed in the development of this project. The report is intended for:

- Academic evaluators assessing the project
- Developers who may extend or maintain the system
- Stakeholders seeking to understand the system's capabilities

## 1.3 Problem Statement

Parking lots in educational institutions, commercial complexes, and public areas face several recurring problems:

1. No visibility into real-time slot availability
2. Manual and error-prone vehicle entry/exit tracking
3. Inconsistent fee calculation leading to revenue leakage
4. No advance reservation capability
5. Absence of analytical reports on revenue and utilization
6. No safeguard against double-booking of the same parking slot
7. No structured mechanism for managing different vehicle types

ParkWise addresses every one of these problems through a structured, layered, and design-pattern-driven Java application.

---

---

# CHAPTER 2: PROJECT OVERVIEW

## 2.1 Project Summary

ParkWise is a single-JVM Java 17 desktop application that provides end-to-end parking management for a parking facility. It communicates with a MySQL 8.x backend through plain JDBC (no ORM frameworks), uses Java Swing for its graphical interface, and is built with Apache Maven.

The project deliberately avoids third-party application frameworks like Spring or Hibernate so that every architectural layer is visible and demonstrates core Java competencies including OOP principles, collections, JDBC, multithreading, I/O streams, serialization, and multiple design patterns.

## 2.2 Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Programming Language | Java (JDK) | 17 |
| User Interface | Java Swing | (JDK built-in) |
| Database | MySQL | 8.x |
| Database Driver | MySQL Connector/J | 8.3.0 |
| Build Tool | Apache Maven | 3.8+ |
| Unit Testing | JUnit 5 (Jupiter) | 5.10.2 |
| Compiler Plugin | maven-compiler-plugin | 3.12.1 |
| Packaging Plugin | maven-jar-plugin | 3.3.0 |
| Test Runner | maven-surefire-plugin | 3.2.5 |

The application runs on any operating system with a JDK 17+ installation (Windows, Linux, macOS) and requires an accessible MySQL 8.x instance.

## 2.3 Key Capabilities

- Secure admin authentication with salted SHA-256 password hashing
- Vehicle registration, search, edit, and deletion
- Parking slot configuration with type-based classification
- Intelligent automatic slot allocation using pluggable strategies
- Real-time vehicle entry and exit with atomic JDBC transactions
- Automatic fee calculation using configurable tariff rates
- Advance reservation management with overlap prevention
- Live dashboard with slot utilization statistics and revenue metrics
- Report generation with date-range filtering and CSV export
- Serialization-based data backup and restore
- Event-driven UI refresh using the Observer pattern
- Concurrent-safe allocation preventing double-booking

---

---

# CHAPTER 3: OBJECTIVES

## 3.1 Primary Objectives

1. **Develop a fully functional parking management system** using Core Java 17 without any application frameworks, demonstrating deep command of the Java standard library.

2. **Implement a clean layered architecture** separating UI, business logic, and data access into distinct, independently maintainable layers.

3. **Demonstrate all major Java syllabus topics** including OOP, Collections Framework, JDBC, Multithreading, I/O Streams, Serialization, Exception Handling, Generics, Lambda Expressions, Stream API, and Wrapper Classes.

4. **Apply four canonical design patterns** — Singleton, Factory, Strategy, and Observer — in real, non-trivial contexts that justify each pattern's use.

5. **Ensure data integrity** through JDBC transactions with atomicity guarantees on every multi-step database operation.

6. **Prevent concurrency bugs** through two-layer protection (JVM `synchronized` block + database conditional UPDATE) preventing double-allocation under concurrent access.

7. **Provide production-grade security** by storing passwords as salted SHA-256 hashes with constant-time comparison to prevent timing attacks.

## 3.2 Secondary Objectives

1. Provide a real-time dashboard that reflects current slot availability and revenue.
2. Enable configurable fee structures per vehicle type.
3. Support three payment methods: Cash, Card, and UPI.
4. Allow advance slot reservations with no-overlap guarantees.
5. Generate and export parking history and revenue reports to CSV.
6. Demonstrate object serialization through a backup and restore feature.
7. Provide comprehensive unit tests covering fee calculation, slot allocation, password utilities, validation, and serialization round-trips.

---

---

# CHAPTER 4: SCOPE AND LIMITATIONS

## 4.1 Scope

The ParkWise system covers the following functional scope:

**In Scope:**
- Administrator account management (single or multiple admins)
- Vehicle registration and management (Two-Wheeler, Four-Wheeler, Accessible)
- Parking slot configuration and status management
- Real-time vehicle entry with automated slot allocation
- Vehicle exit with fee calculation and payment processing
- Advance reservation with time-range overlap detection
- Tariff configuration by vehicle type
- Dashboard with live statistics
- Reporting: parking history, daily revenue, monthly revenue, average duration, most-used slots
- CSV export of session data and revenue reports
- Data backup via Java object serialization
- Strategy-switchable slot allocation (First Available vs. Best Fit)
- Password change for logged-in administrators

**Out of Scope:**
- Multi-location parking management
- Automated number plate recognition (ANPR)
- Mobile application or web interface
- Real-time SMS or email notifications to vehicle owners
- Multi-currency support
- Integration with external payment gateways

## 4.2 Limitations

1. The application runs on a single JVM. While the concurrency model handles multiple threads within the same JVM, it does not natively support distributed deployment across multiple application servers (though the database-level conditional UPDATE provides partial protection even in that case).

2. The serialization-based backup (Settings → Create Backup) is a syllabus demonstration of Java object serialization. It backs up in-memory snapshots of vehicles and slots and is not a replacement for a proper MySQL dump.

3. The SHA-256 password hashing, while far superior to plaintext, is not a state-of-the-art password hashing function. Production systems should use bcrypt, scrypt, or Argon2.

4. The connection pool is a simple home-built implementation. Production systems would benefit from HikariCP or a similar battle-tested pool.

---

---

# CHAPTER 5: SYSTEM REQUIREMENTS

## 5.1 Hardware Requirements

| Component | Minimum | Recommended |
|---|---|---|
| Processor | 1 GHz Dual Core | 2 GHz Quad Core |
| RAM | 512 MB | 2 GB |
| Disk Space | 200 MB | 1 GB |
| Display | 1024 × 768 | 1280 × 800 or higher |

## 5.2 Software Requirements

| Requirement | Minimum Version | Notes |
|---|---|---|
| Java Development Kit | JDK 17 | Must support Java 17 language features |
| Java Runtime Environment | JRE 17 | Included with JDK |
| MySQL Server | 8.0 | Database backend |
| Apache Maven | 3.8 | For building from source |
| Operating System | Windows 10 / Ubuntu 20.04 / macOS 12 | Any OS with JDK 17 |

## 5.3 Dependencies

The project has exactly two runtime/test dependencies declared in `pom.xml`:

1. **com.mysql:mysql-connector-j:8.3.0** — JDBC driver for MySQL connectivity
2. **org.junit.jupiter:junit-jupiter:5.10.2** — JUnit 5 testing framework (test scope only)

No Spring, Hibernate, Lombok, Jackson, or any other third-party framework is used.

---

---

# CHAPTER 6: LITERATURE REVIEW / BACKGROUND

## 6.1 Parking Management Systems

Traditional parking management relies on manual ticketing, attendant-based slot assignment, and paper-based records. The limitations of such systems — including lack of real-time visibility, human errors in fee calculation, and the inability to generate analytical reports — have driven the development of computerized parking management systems (CPMS).

A modern CPMS must handle:
- Concurrent access (multiple vehicles arriving simultaneously)
- Fair and efficient slot allocation
- Accurate fee computation
- Persistent records for auditing and reporting
- Prevention of double-booking

## 6.2 Java as a Platform for Desktop Applications

Java has been a dominant platform for enterprise and desktop development since its introduction in 1995. The Java Virtual Machine (JVM) provides platform independence, while the standard library provides rich APIs for I/O, networking, concurrency, and UI development.

Java Swing is a mature, lightweight GUI toolkit that provides complete control over the application's look and feel. While newer frameworks like JavaFX exist, Swing remains widely used for academic projects due to its accessibility and the complete availability of all components within the JDK.

## 6.3 Design Patterns

Design patterns are reusable solutions to commonly occurring software design problems, formalized by the "Gang of Four" (Gamma, Helm, Johnson, Vlissides) in their 1994 book "Design Patterns: Elements of Reusable Object-Oriented Software."

ParkWise implements four canonical patterns:

- **Singleton** — Ensures a class has only one instance and provides global access to it (used for DBConnection, AppConfig, ParkingEventPublisher)
- **Factory Method** — Defines an interface for creating objects but lets subclasses decide which class to instantiate (used for NotificationFactory)
- **Strategy** — Defines a family of algorithms, encapsulates each one, and makes them interchangeable (used for slot allocation and fee calculation)
- **Observer** — Defines a one-to-many dependency so that when one object changes state, all dependents are notified (used for event-driven dashboard refresh)

## 6.4 JDBC and Database Transactions

JDBC (Java Database Connectivity) is the standard Java API for relational database access. It provides a consistent interface regardless of the underlying database vendor. ParkWise uses JDBC exclusively — no ORM is involved. This ensures that all SQL is explicit, all queries are visible in the DAO layer, and all transactions are managed precisely.

ACID properties (Atomicity, Consistency, Isolation, Durability) are maintained through `setAutoCommit(false)`, explicit `commit()`, and `rollback()` calls in all multi-step database operations.

---

---

# CHAPTER 7: SYSTEM ARCHITECTURE

## 7.1 Architectural Overview

ParkWise follows a classic four-layer architecture:

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer (Swing)                     │
│  LoginScreen, MainFrame, DashboardPanel, VehiclePanel,  │
│  SlotPanel, EntryExitPanel, ReservationPanel,           │
│  TariffPanel, ReportPanel, SettingsPanel                │
└─────────────────────────────────────────────────────────┘
                       │ calls
                       ▼
┌─────────────────────────────────────────────────────────┐
│                 Service Layer (Business Logic)           │
│  AuthService, VehicleService, ParkingSlotService,       │
│  AllocationService, ParkingService, ReservationService, │
│  TariffService                                          │
│  + strategy, observer, factory sub-packages             │
└─────────────────────────────────────────────────────────┘
                       │ delegates to
                       ▼
┌─────────────────────────────────────────────────────────┐
│                   DAO Layer (JDBC)                       │
│  AdminDAO, VehicleDAO, ParkingSlotDAO,                  │
│  ParkingSessionDAO, ReservationDAO, TariffDAO,          │
│  PaymentDAO                                             │
└─────────────────────────────────────────────────────────┘
                       │ uses
                       ▼
┌─────────────────────────────────────────────────────────┐
│            Util Layer (Cross-cutting Concerns)           │
│  DBConnection (Singleton pool), AppConfig,              │
│  ValidationUtil, PasswordUtil, DatabaseInitializer,     │
│  DashboardRefreshTask                                   │
└─────────────────────────────────────────────────────────┘
                       │ connects to
                       ▼
┌─────────────────────────────────────────────────────────┐
│                MySQL Database (parkwise_db)              │
│  admins, vehicles, parking_slots, tariffs,              │
│  parking_sessions, reservations, payments               │
└─────────────────────────────────────────────────────────┘
```

## 7.2 Package Structure

| Package | Responsibility |
|---|---|
| `com.parkwise` | Root package, contains `Main.java` entry point |
| `com.parkwise.model` | Plain Java entity classes mirroring database tables |
| `com.parkwise.dao` | One DAO per table — all SQL lives here using PreparedStatement |
| `com.parkwise.service` | Business logic, transaction orchestration, event publishing |
| `com.parkwise.strategy` | Pluggable algorithms: slot selection, fee calculation |
| `com.parkwise.observer` | Event publisher and listener interface for UI decoupling |
| `com.parkwise.factory` | NotificationFactory creating notification implementations |
| `com.parkwise.exception` | Domain-specific checked exceptions |
| `com.parkwise.util` | Connection pool, config, validation, password hashing, threading |
| `com.parkwise.report` | Report generation and CSV export via character streams |
| `com.parkwise.backup` | Object serialization backup and restore |
| `com.parkwise.ui` | Swing screens — zero SQL, zero business logic |

## 7.3 Architectural Principles

**Separation of Concerns:** Every layer has a single, well-defined responsibility. The UI layer contains no SQL. The DAO layer contains no business rules. The Service layer orchestrates but does not render.

**No SQL in UI:** Every DAO owns its SQL. The UI talks only to services, which talk only to DAOs. This ensures that changing the database schema requires modifying only the DAO layer.

**Swappable Strategies:** Switching from `FirstAvailableStrategy` to `BestFitStrategy` requires no code change in `AllocationService` — only a configuration update in `AppConfig`.

**Transaction Integrity:** Every multi-step database operation uses `setAutoCommit(false)` → operations → `commit()`, with `rollback()` in the `catch` block and connection release in `finally`.

**Testability:** Strategies and utilities are pure functions with no database dependency. They can be unit-tested in complete isolation, as demonstrated in the test suite.

## 7.4 Startup Sequence

1. JVM loads `Main.java` and sets the system look-and-feel.
2. `DatabaseInitializer.initialize()` runs — an idempotent operation that creates the default admin account and tariff entries if they do not already exist.
3. `SwingUtilities.invokeLater()` launches `LoginScreen` on the Event Dispatch Thread (EDT).
4. On successful authentication via `AuthService`, `LoginScreen` disposes itself and constructs `MainFrame`.
5. `MainFrame` registers an Observer with `ParkingEventPublisher` for dashboard updates and starts the `DashboardRefreshTask` background scheduler.

---

---

# CHAPTER 8: DATABASE DESIGN

## 8.1 Entity-Relationship Overview

The ParkWise database (`parkwise_db`) consists of seven tables:

1. **admins** — Administrator accounts
2. **vehicles** — Registered vehicles
3. **parking_slots** — Physical parking slots
4. **tariffs** — Fee configuration per vehicle type
5. **parking_sessions** — Vehicle entry/exit records
6. **reservations** — Advance bookings
7. **payments** — Payment transaction records

## 8.2 Table Descriptions

### 8.2.1 admins

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | INT | PK, AUTO_INCREMENT | Primary key |
| username | VARCHAR(50) | NOT NULL, UNIQUE | Admin login name |
| password_hash | VARCHAR(255) | NOT NULL | Salted SHA-256 hash (salt:hash) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Account creation time |

Passwords are stored as `salt:hash` where the salt is a 16-byte random hex string and the hash is SHA-256(salt + password). Plaintext passwords are never stored.

### 8.2.2 vehicles

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | INT | PK, AUTO_INCREMENT | Primary key |
| registration_number | VARCHAR(20) | NOT NULL, UNIQUE | Vehicle registration plate |
| vehicle_type | ENUM | NOT NULL | TWO_WHEELER / FOUR_WHEELER / ACCESSIBLE |
| owner_name | VARCHAR(100) | NOT NULL | Owner's full name |
| contact_number | VARCHAR(15) | NOT NULL | 10-digit mobile number |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Registration time |

Indexed on `registration_number` and `vehicle_type` for fast lookups.

### 8.2.3 parking_slots

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | INT | PK, AUTO_INCREMENT | Primary key |
| slot_number | VARCHAR(10) | NOT NULL, UNIQUE | Human-readable slot ID (e.g. "A01") |
| slot_type | ENUM | NOT NULL | TWO_WHEELER / FOUR_WHEELER / ACCESSIBLE |
| slot_status | ENUM | NOT NULL, DEFAULT 'AVAILABLE' | AVAILABLE / OCCUPIED / RESERVED / MAINTENANCE |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | Soft-delete flag |

Indexed on `slot_type`, `slot_status`, and `active`.

### 8.2.4 tariffs

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | INT | PK, AUTO_INCREMENT | Primary key |
| vehicle_type | ENUM | NOT NULL, UNIQUE | Vehicle type this tariff applies to |
| first_hour_fee | DECIMAL(10,2) | NOT NULL | Base fee for first 60 minutes |
| additional_hour_fee | DECIMAL(10,2) | NOT NULL | Fee per additional started hour |
| active | TINYINT(1) | NOT NULL, DEFAULT 1 | Active flag |

`DECIMAL(10,2)` is used for monetary values to avoid floating-point precision errors.

### 8.2.5 parking_sessions

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | INT | PK, AUTO_INCREMENT | Primary key |
| vehicle_id | INT | FK → vehicles.id | Owning vehicle |
| slot_id | INT | FK → parking_slots.id | Assigned slot |
| entry_time | DATETIME | NOT NULL | When vehicle entered |
| exit_time | DATETIME | NULL | When vehicle exited (null while ACTIVE) |
| duration_minutes | INT | NULL | Total parking duration |
| fee_amount | DECIMAL(10,2) | NULL | Calculated fee |
| payment_status | ENUM | NOT NULL, DEFAULT 'PENDING' | PENDING / PAID / WAIVED |
| session_status | ENUM | NOT NULL, DEFAULT 'ACTIVE' | ACTIVE / COMPLETED / CANCELLED |

Indexed on `(vehicle_id, session_status)` for fast active-session lookups.

### 8.2.6 reservations

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | INT | PK, AUTO_INCREMENT | Primary key |
| vehicle_id | INT | FK → vehicles.id | Vehicle that reserved |
| slot_id | INT | FK → parking_slots.id | Reserved slot |
| start_time | DATETIME | NOT NULL | Reservation window start |
| end_time | DATETIME | NOT NULL | Reservation window end |
| reservation_status | ENUM | NOT NULL, DEFAULT 'CONFIRMED' | CONFIRMED / CANCELLED / COMPLETED |
| created_at | TIMESTAMP | NOT NULL | When reservation was made |

A CHECK constraint (`end_time > start_time`) is enforced at the database level.

### 8.2.7 payments

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | INT | PK, AUTO_INCREMENT | Primary key |
| session_id | INT | FK → parking_sessions.id | Associated parking session |
| amount | DECIMAL(10,2) | NOT NULL | Amount paid |
| payment_method | ENUM | NOT NULL | CASH / CARD / UPI / WAIVED |
| payment_status | ENUM | NOT NULL, DEFAULT 'COMPLETED' | PENDING / COMPLETED / FAILED |
| paid_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Payment timestamp |

## 8.3 Default Sample Data

The `sample_data.sql` file seeds the database with:
- **22 parking slots**: 10 Two-Wheeler (A01–A10), 10 Four-Wheeler (B01–B10), 2 Accessible (C01–C02)
- **3 tariff records**: Two-Wheeler (₹20/hr + ₹10/add.), Four-Wheeler (₹40/hr + ₹20/add.), Accessible (₹20/hr + ₹10/add.)
- **7 sample vehicles**: mix of TWO_WHEELER, FOUR_WHEELER, and ACCESSIBLE types

The default admin account (username: `admin`, password: `Admin@123`) is created programmatically by `DatabaseInitializer.java` using `PasswordUtil.hashPassword()` to ensure the stored hash matches the verification logic.

## 8.4 Referential Integrity

All foreign key relationships enforce `ON DELETE RESTRICT`, meaning a vehicle cannot be deleted if it has parking sessions or reservations, and a slot cannot be deleted if it is referenced by sessions or reservations. This prevents orphaned records.

---

---

# CHAPTER 9: DESIGN PATTERNS USED

## 9.1 Singleton Pattern

**Where used:** `DBConnection`, `AppConfig`, `ParkingEventPublisher`

**Purpose:** Ensures exactly one instance of each of these classes exists in the JVM, providing a single controlled point of access.

**Implementation detail:**

All three classes use double-checked locking with a `volatile` field for thread-safe lazy initialization:

```java
private static volatile DBConnection instance;

public static DBConnection getInstance() {
    if (instance == null) {
        synchronized (DBConnection.class) {
            if (instance == null) {
                instance = new DBConnection();
            }
        }
    }
    return instance;
}
```

`volatile` ensures that the partially-constructed object is never visible to another thread. The outer null check avoids synchronization overhead on every call after initialization.

**Why Singleton is appropriate here:**
- `DBConnection` manages a connection pool — a shared, stateful resource that must be coordinated.
- `AppConfig` loads configuration from a file — expensive to re-read, consistent once loaded.
- `ParkingEventPublisher` is an event bus — listeners register once and must receive all events.

## 9.2 Factory Pattern

**Where used:** `NotificationFactory`

**Purpose:** Centralizes the creation of `NotificationService` implementations, decoupling clients from concrete types.

```java
public static NotificationService create(String channel) {
    return switch (channel.toUpperCase()) {
        case "DIALOG" -> new DialogNotificationService();
        case "LOG"    -> new LogNotificationService();
        default       -> throw new IllegalArgumentException(
                            "Unknown notification channel: " + channel);
    };
}
```

The client requests a notification service by channel name (e.g., `"DIALOG"`, `"LOG"`). The factory decides which class to instantiate. Adding a new notification channel (e.g., email) requires only adding a new case to the factory — no existing code changes.

## 9.3 Strategy Pattern

**Where used:** `SlotAllocationStrategy` (slot selection), `ParkingFeeStrategy` (fee calculation)

**Purpose:** Defines a family of algorithms, encapsulates each one, and makes them interchangeable at runtime.

### Slot Allocation Strategies

**Interface:** `SlotAllocationStrategy`
```java
public interface SlotAllocationStrategy {
    ParkingSlot selectSlot(List<ParkingSlot> availableSlots);
}
```

**Implementations:**
- `FirstAvailableStrategy` — Returns the slot with the lexicographically smallest slot number (e.g., "A01" before "A02")
- `BestFitStrategy` — Returns the most type-compatible slot with consistent tie-breaking based on slot number ordering

The active strategy is resolved at runtime from `AppConfig`:
```java
private SlotAllocationStrategy resolveStrategy() {
    return switch (AppConfig.getInstance().getAllocationStrategy()) {
        case "BEST_FIT" -> new BestFitStrategy();
        default         -> new FirstAvailableStrategy();
    };
}
```

### Fee Calculation Strategy

**Interface:** `ParkingFeeStrategy`
```java
public interface ParkingFeeStrategy {
    BigDecimal calculate(int durationMinutes, Tariff tariff);
}
```

**Implementation:** `StandardFeeStrategy` applies the first-hour flat fee and adds a ceiling-division-based charge for each additional started hour. Using an interface means alternative fee models (e.g., weekend rates, flat daily cap) can be plugged in without changing `ParkingService`.

## 9.4 Observer Pattern

**Where used:** `ParkingEventPublisher` / `ParkingEventListener`

**Purpose:** Defines a one-to-many dependency so that when one object changes state, all registered dependents are notified automatically.

**Event types:**
- `VEHICLE_ENTERED` — published by `AllocationService` after successful slot allocation
- `VEHICLE_EXITED` — published by `ParkingService` after successful exit processing
- `ALLOCATION_FAILED` — published when no slot is available
- `RESERVATION_CREATED` — published by `ReservationService`
- `RESERVATION_CANCELLED` — published by `ReservationService`
- `DASHBOARD_REFRESH` — published by `DashboardRefreshTask`

**Thread safety:** `ParkingEventPublisher` uses a `CopyOnWriteArrayList` for its listener collection. This allows safe iteration (for event publishing) even if a listener is added or removed concurrently on another thread.

**Swing safety:** The `MainFrame` registers a listener that routes dashboard refresh calls through `SwingUtilities.invokeLater()`, ensuring all Swing component updates happen on the Event Dispatch Thread (EDT).

```java
ParkingEventPublisher.getInstance().subscribe(event -> {
    SwingUtilities.invokeLater(() -> {
        if (event.getEventType() == ParkingEvent.EventType.VEHICLE_ENTERED ||
            event.getEventType() == ParkingEvent.EventType.VEHICLE_EXITED) {
            dashboardPanel.refresh();
        }
    });
});
```

---

---

# CHAPTER 10: MODULE-WISE DESCRIPTION

## 10.1 Authentication Module

**Classes involved:** `AuthService`, `AdminDAO`, `Admin`, `PasswordUtil`, `LoginScreen`

**Functionality:**
The authentication module manages the lifecycle of an admin session. When a user submits credentials through `LoginScreen`, the `AuthService.login()` method:
1. Validates that the username and password are non-blank
2. Queries `AdminDAO.findByUsername()` to retrieve the stored admin record
3. Calls `PasswordUtil.verifyPassword()` to compare the submitted password against the stored `salt:hash`
4. On success, sets the `currentAdmin` reference in `AuthService`

The `AuthService` maintains the current session through an in-memory `Admin` reference. Logging out clears this reference.

Password changes are handled by `AuthService.changePassword()` which first verifies the old password, then calls `PasswordUtil.hashPassword()` to generate a new hash, and updates the database via `AdminDAO.updatePassword()`.

## 10.2 Vehicle Management Module

**Classes involved:** `VehicleService`, `VehicleDAO`, `Vehicle`, `VehicleType`, `VehiclePanel`, `ValidationUtil`

**Functionality:**
This module manages the lifecycle of registered vehicles. `VehicleService` maintains an in-memory `HashMap<String, Vehicle>` cache keyed on registration number for O(1) lookups without repeated database roundtrips.

Key operations:
- **Add Vehicle:** Validates registration format (Indian plate format, e.g., KA01AB1234), owner name, and contact number. Checks for duplicates before inserting.
- **Find by Registration:** Checks HashMap cache first; falls back to `VehicleDAO` on cache miss.
- **Search:** Uses `VehicleDAO.search()` for database-side pattern matching, then deduplicates using a `HashSet` via the Stream API.
- **Update:** Validates mutable fields (owner name, contact) and updates both database and cache.
- **Delete:** Removes from database and evicts from cache using `removeIf` on the entry set.

`VehicleType` is an enum with three values: `TWO_WHEELER`, `FOUR_WHEELER`, `ACCESSIBLE`. Each has a human-readable `getDisplayName()` method.

## 10.3 Slot Management Module

**Classes involved:** `ParkingSlotService`, `ParkingSlotDAO`, `ParkingSlot`, `SlotStatus`, `SlotPanel`

**Functionality:**
Manages the physical parking slots of the facility. Slots have a type (matching vehicle types) and a status (AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE) plus an active flag.

Key operations:
- **Add Slot:** Validates slot number format (e.g., "A01"), ensures no duplicate, inserts with AVAILABLE status.
- **Filter by Status:** Uses the Stream API — `slotDAO.findAll().stream().filter(s -> s.getSlotStatus() == status).sorted(...).collect(...)`.
- **Dashboard Counts:** `slotDAO.countByStatus()` returns an array of [total, available, occupied, reserved] for the dashboard.
- **Deactivation Guard:** Cannot deactivate a slot that is currently OCCUPIED or RESERVED — enforced in `ParkingSlotService.updateSlot()`.

`ParkingSlot` implements both `Serializable` (for backup) and `Cloneable` (for defensive copying), with a copy constructor demonstrated as part of the cloning pattern.

## 10.4 Allocation and Entry Module

**Classes involved:** `AllocationService`, `ParkingSessionDAO`, `ParkingSlotDAO`, `SlotAllocationStrategy`, `ParkingEventPublisher`, `EntryExitPanel`

**Functionality:**
This is the most concurrency-sensitive module. `AllocationService.allocateAndEnter()` performs:

1. Acquires the JVM-level `ALLOCATION_LOCK` (`synchronized` block)
2. Checks for an existing active session for the vehicle
3. Opens a JDBC transaction (`setAutoCommit(false)`)
4. Fetches available slots of the matching type
5. Delegates slot selection to the configured `SlotAllocationStrategy`
6. Issues a conditional UPDATE: `UPDATE parking_slots SET slot_status='OCCUPIED' WHERE id=? AND slot_status='AVAILABLE' AND active=1`
7. If the UPDATE returns 0 rows affected, the slot was claimed by another transaction — rolls back and throws `SlotNotAvailableException`
8. Creates and inserts a new `ParkingSession` record
9. Commits the transaction
10. Publishes a `VEHICLE_ENTERED` event

The entire operation is wrapped in try/catch/finally to guarantee rollback on failure and connection release on completion.

## 10.5 Exit and Payment Module

**Classes involved:** `ParkingService`, `ParkingSessionDAO`, `PaymentDAO`, `ParkingSlotDAO`, `TariffService`, `StandardFeeStrategy`

**Functionality:**
`ParkingService.processExit()` performs vehicle checkout atomically:

1. Loads the active session and verifies it exists and is in ACTIVE status
2. Calculates exit time using `LocalDateTime.now()` and duration using `java.time.Duration.between(entryTime, exitTime).toMinutes()`
3. Retrieves the applicable tariff via `TariffService.getTariffForType()`
4. Calculates the fee using `StandardFeeStrategy.calculate(durationMinutes, tariff)`
5. Opens a JDBC transaction:
   - Updates `parking_sessions`: sets exit time, duration, fee, status=COMPLETED, payment_status=PAID
   - Inserts into `payments`: amount, method, status=COMPLETED
   - Updates `parking_slots`: sets slot_status=AVAILABLE
6. Commits (or rolls back on any failure)
7. Publishes a `VEHICLE_EXITED` event

`EntryExitPanel` uses `SwingWorker` to perform the exit operation off the EDT, then displays a formatted receipt in a dialog on `done()`.

## 10.6 Reservation Module

**Classes involved:** `ReservationService`, `ReservationDAO`, `ParkingSlotDAO`, `ReservationPanel`

**Functionality:**
`ReservationService.createReservation()`:
1. Validates that end_time is after start_time and start_time is not in the past
2. Opens a JDBC transaction
3. If slotId is 0, auto-allocates a slot using the configured strategy
4. Calls `ReservationDAO.hasOverlap()` within the transaction to detect time-range conflicts
5. Updates the slot to RESERVED status
6. Inserts the reservation record
7. Commits
8. Publishes a `RESERVATION_CREATED` event

Cancellation (`cancelReservation()`) updates the reservation status to CANCELLED and releases the slot back to AVAILABLE in a single transaction.

## 10.7 Tariff Management Module

**Classes involved:** `TariffService`, `TariffDAO`, `Tariff`, `TariffPanel`

**Functionality:**
Provides CRUD operations for the per-vehicle-type fee configuration. `TariffService.updateTariff()` validates that neither `firstHourFee` nor `additionalHourFee` are negative before delegating to `TariffDAO.update()`. Fee values are stored and handled as `java.math.BigDecimal` throughout to avoid floating-point precision errors in monetary calculations.

## 10.8 Dashboard Module

**Classes involved:** `DashboardPanel`, `ParkingSlotService`, `ParkingService`, `DashboardRefreshTask`, `ParkingEventPublisher`

**Functionality:**
The dashboard displays:
- Total / Available / Occupied / Reserved slot counts
- Number of sessions completed today
- Today's revenue (in ₹)

Data is refreshed in two ways:
1. **Event-driven:** The `MainFrame` Observer receives `VEHICLE_ENTERED`, `VEHICLE_EXITED`, and `DASHBOARD_REFRESH` events and calls `dashboardPanel.refresh()`.
2. **Scheduled:** `DashboardRefreshTask` uses a `ScheduledExecutorService` with daemon threads to publish a `DASHBOARD_REFRESH` event every 30 seconds, triggering a database refresh even when no vehicle events occur.

## 10.9 Reports Module

**Classes involved:** `ReportService`, `ReportPanel`, `ParkingSessionDAO`, `PaymentDAO`

**Functionality:**
`ReportService` provides:
- **Parking History:** Sessions in a date range, sorted by entry time descending
- **Daily Revenue:** `TreeMap<LocalDate, BigDecimal>` — uses `Collectors.groupingBy` + `Collectors.reducing`
- **Monthly Revenue:** `TreeMap<String, BigDecimal>` — grouped by "yyyy-MM" format
- **Long Duration Sessions:** Sessions with duration exceeding a configurable threshold
- **Most Used Slots:** Top N slots by session count using `groupingBy + counting + sorted + limit`
- **Average Duration:** `mapToInt(getDurationMinutes).average()` returning `OptionalDouble`
- **CSV Export:** `BufferedWriter(FileWriter)` — character stream I/O for session data and revenue reports

## 10.10 Backup Module

**Classes involved:** `BackupService`, `BackupSnapshot`, `SettingsPanel`

**Functionality:**
`BackupService.createBackup()`:
1. Loads current vehicles and slots from the database
2. Creates a `BackupSnapshot` object
3. Serializes it using `ObjectOutputStream(FileOutputStream)` to a timestamped `.bak` file

`BackupService.restoreBackup()`:
1. Reads the file using `ObjectInputStream(FileInputStream)`
2. Casts the deserialized object to `BackupSnapshot`
3. Returns the snapshot to the caller

`BackupSnapshot` and all objects it contains (`Vehicle`, `ParkingSlot`) implement `java.io.Serializable` with explicit `serialVersionUID` values.

---

---

# CHAPTER 11: CLASS DESCRIPTIONS

## 11.1 Model Classes

### Vehicle
**Package:** `com.parkwise.model`  
**Implements:** `Serializable`  
**Fields:** `id`, `registrationNumber`, `vehicleType` (VehicleType enum), `ownerName`, `contactNumber`, `createdAt`  
**Purpose:** Plain Java object representing a registered vehicle. Serializable for backup inclusion.

### ParkingSlot
**Package:** `com.parkwise.model`  
**Implements:** `Serializable`, `Cloneable`  
**Fields:** `id`, `slotNumber`, `slotType` (VehicleType), `slotStatus` (SlotStatus), `active`  
**Purpose:** Represents a physical parking slot. Implements both Serializable and Cloneable; includes a defensive copy constructor.

### ParkingSession
**Package:** `com.parkwise.model`  
**Fields:** `id`, `vehicleId`, `slotId`, `entryTime`, `exitTime`, `durationMinutes`, `feeAmount` (BigDecimal), `paymentStatus`, `sessionStatus`, plus denormalized display fields (`registrationNumber`, `slotNumber`, `vehicleType`)  
**Inner enums:** `SessionStatus {ACTIVE, COMPLETED, CANCELLED}`, `PaymentStatus {PENDING, PAID, WAIVED}`  
**Purpose:** Records one vehicle parking session. `exitTime`, `durationMinutes`, and `feeAmount` are null while session is ACTIVE.

### Tariff
**Package:** `com.parkwise.model`  
**Fields:** `id`, `vehicleType`, `firstHourFee` (BigDecimal), `additionalHourFee` (BigDecimal), `active`  
**Purpose:** Configurable fee structure per vehicle type. Uses BigDecimal for monetary precision.

### Reservation
**Package:** `com.parkwise.model`  
**Fields:** `id`, `vehicleId`, `slotId`, `startTime`, `endTime`, `reservationStatus`, `createdAt`, plus denormalized `registrationNumber`, `slotNumber`, `ownerName`  
**Inner enum:** `ReservationStatus {CONFIRMED, CANCELLED, COMPLETED}`  
**Purpose:** Represents an advance parking booking.

### Payment
**Package:** `com.parkwise.model`  
**Fields:** `id`, `sessionId`, `amount` (BigDecimal), `paymentMethod`, `paymentStatus`, `paidAt`  
**Inner enums:** `PaymentMethod {CASH, CARD, UPI, WAIVED}`, `PaymentStatus {PENDING, COMPLETED, FAILED}`  
**Purpose:** Records a payment transaction linked to a completed session.

### Admin
**Package:** `com.parkwise.model`  
**Fields:** `id`, `username`, `passwordHash`, `createdAt`  
**Purpose:** Administrator account. `passwordHash` stores the salted SHA-256 hash.

### SlotStatus (Enum)
Values: `AVAILABLE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`  
Each value has a `getDisplayName()` method returning the human-readable label.

### VehicleType (Enum)
Values: `TWO_WHEELER`, `FOUR_WHEELER`, `ACCESSIBLE`  
Each value has a `getDisplayName()` method.

## 11.2 DAO Classes

Each DAO follows the same pattern: JDBC PreparedStatement for all queries, result set mapping in a private helper method, and explicit connection management (connections are passed in for transactional operations or obtained/released for standalone operations).

### AdminDAO
Methods: `findByUsername(String)`, `insert(Admin)`, `updatePassword(int, String)`

### VehicleDAO
Methods: `findAll()`, `findById(int)`, `findByRegistrationNumber(String)`, `existsByRegistrationNumber(String)`, `insert(Vehicle)`, `update(Vehicle)`, `delete(int)`, `search(String)`

### ParkingSlotDAO
Methods: `findAll()`, `findById(int)`, `findAvailableByType(VehicleType)`, `existsBySlotNumber(String)`, `insert(ParkingSlot)`, `update(ParkingSlot)`, `updateStatus(Connection, int, SlotStatus)`, `countByStatus()`

### ParkingSessionDAO
Methods: `findById(int)`, `findAll()`, `findActiveByVehicleId(int)`, `findAllActive()`, `findByDateRange(LocalDate, LocalDate)`, `insert(Connection, ParkingSession)`, `completeSession(Connection, int, Timestamp, int, BigDecimal, PaymentStatus)`, `countCompletedToday()`, `getTodayRevenue()`

### ReservationDAO
Methods: `findAll()`, `findById(int)`, `findByStatus(ReservationStatus)`, `hasOverlap(Connection, int, LocalDateTime, LocalDateTime, int)`, `insert(Connection, Reservation)`, `cancel(int)`

### TariffDAO
Methods: `findAll()`, `findByVehicleType(VehicleType)`, `update(Tariff)`

### PaymentDAO
Methods: `insert(Connection, Payment)`, `findBySessionId(int)`

## 11.3 Service Classes

### AuthService
Manages admin login, logout, and password change. Maintains an in-memory `currentAdmin` reference for the active session. Uses `PasswordUtil` for hash verification.

### VehicleService
Business logic for vehicle CRUD. Maintains a `HashMap<String, Vehicle>` in-memory cache for registration-based lookups. Uses Stream API for search deduplication.

### ParkingSlotService
Business logic for slot management. Provides stream-based filtering and sorted results. Enforces deactivation guard for occupied/reserved slots.

### AllocationService
Core entry service with two-layer concurrency protection. Resolves allocation strategy at runtime from `AppConfig`. Performs atomic DB transaction for slot claiming and session creation.

### ParkingService
Core exit service. Calculates duration using `java.time.Duration`, resolves tariff and fee, performs atomic three-step DB transaction (session update + payment insert + slot release).

### ReservationService
Manages advance bookings. Performs overlap detection within a transaction. Handles both specific slot selection and automatic allocation.

### TariffService
Retrieves and updates tariff records. Validates that fee amounts are non-negative.

## 11.4 Utility Classes

### DBConnection
Singleton connection pool. Uses double-checked locking with `volatile` for thread-safe initialization. Maintains an `ArrayDeque<Connection>` as a simple pool. Pre-warms 2 connections on startup.

### PasswordUtil
Static utility for salted SHA-256 hashing. `hashPassword()` generates a random 16-byte salt and returns `salt:hash`. `verifyPassword()` re-derives the hash and compares using `constantTimeEquals()` (XOR-based comparison to prevent timing attacks).

### ValidationUtil
Static validation methods: `isBlank()`, `isValidRegistrationNumber()` (regex), `isValidContactNumber()` (10-digit regex), `isValidSlotNumber()` (letter + 2-digit regex), `require()` (precondition assertion).

### AppConfig
Singleton configuration store. Loads from a properties file (or defaults). Provides `getAllocationStrategy()`, `APP_NAME`, `EXPORT_DIR`, `BACKUP_DIR`, and `DATETIME_FORMAT` constants.

### DatabaseInitializer
Idempotent startup initializer. Creates the default admin account using `PasswordUtil.hashPassword()` if no admin exists. Inserts default tariffs if absent.

### DashboardRefreshTask
Wraps a `ScheduledExecutorService` that publishes a `DASHBOARD_REFRESH` event on a configurable interval. Uses daemon threads so they do not prevent JVM shutdown. Provides `start(intervalSeconds)` and `shutdown()` methods.

## 11.5 Strategy Classes

### SlotAllocationStrategy (Interface)
Single method: `ParkingSlot selectSlot(List<ParkingSlot> availableSlots)`

### FirstAvailableStrategy
Returns the slot with the smallest slot number (natural string ordering). Deterministic and simple.

### BestFitStrategy
Sorts slots by type compatibility score and slot number for consistent tie-breaking. Returns the most type-compatible slot.

### ParkingFeeStrategy (Interface)
Single method: `BigDecimal calculate(int durationMinutes, Tariff tariff)`

### StandardFeeStrategy
Applies first-hour flat fee; for durations > 60 minutes, computes additional hours using ceiling division: `additionalHours = (additionalMinutes + 59) / 60`. Uses `BigDecimal.multiply()` and `BigDecimal.add()` for precision.

## 11.6 Observer Classes

### ParkingEventPublisher
Singleton event bus. Uses `CopyOnWriteArrayList` for thread-safe listener management. `publish()` iterates all listeners, catching individual listener exceptions so one bad listener cannot break the event chain.

### ParkingEventListener (Interface)
Single method: `void onEvent(ParkingEvent event)`

### ParkingEvent
Carries `EventType`, a message string, and an optional payload object. `EventType` is an enum: `VEHICLE_ENTERED`, `VEHICLE_EXITED`, `ALLOCATION_FAILED`, `RESERVATION_CREATED`, `RESERVATION_CANCELLED`, `DASHBOARD_REFRESH`.

## 11.7 Factory Classes

### NotificationService (Interface)
Methods: `notifyInfo(String title, String message)`, `notifyError(String title, String message)`

### NotificationFactory
Static factory with `create(String channel)` and `createDefault()`. Supports `"DIALOG"` (Swing JOptionPane) and `"LOG"` (java.util.logging.Logger) channels.

### DialogNotificationService
Shows `JOptionPane.showMessageDialog()` for info and error notifications.

### LogNotificationService
Logs notifications using `java.util.logging.Logger` (useful for headless or test contexts).

## 11.8 Exception Classes

| Exception | Extends | When Thrown |
|---|---|---|
| `DuplicateVehicleException` | `Exception` | Registration number already exists |
| `VehicleNotFoundException` | `Exception` | No vehicle with given registration |
| `SlotNotAvailableException` | `Exception` | No slot available for vehicle type |
| `InvalidParkingSessionException` | `Exception` | Session not found or wrong state |
| `InvalidTariffException` | `Exception` | Negative or missing tariff values |
| `ReservationOverlapException` | `Exception` | Time range conflicts with existing reservation |

All are checked exceptions, forcing callers to handle them explicitly.

---

---

# CHAPTER 12: USER INTERFACE DESIGN

## 12.1 UI Architecture

The UI layer consists entirely of Java Swing components. The architecture enforces zero SQL and zero business logic in UI classes. Every user action delegates to the appropriate service class, typically via `SwingWorker` for non-blocking background execution.

The application follows a sidebar-navigation pattern: `MainFrame` contains a fixed sidebar on the left and a `CardLayout` content area on the right. Clicking a navigation item swaps the displayed panel.

## 12.2 Screen Inventory

### LoginScreen
A centered login dialog with a username field, password field (echo character masked), and a Login button. Executes authentication via `AuthService.login()`. On success, disposes itself and creates `MainFrame`. On failure, shows an error label. Look-and-feel uses the system default for a native appearance.

### MainFrame
The main application window (minimum 1100×700, preferred 1280×800). Contains:
- Sidebar with navigation buttons (emoji icons + labels) for 8 sections
- A `CardLayout` content area
- Logged-in username display at the bottom of the sidebar
- Logout and exit handlers with confirmation dialogs
- Observer registration for `ParkingEventPublisher` events
- `DashboardRefreshTask` lifecycle management

### DashboardPanel
Displays a 2×3 grid of statistic cards:
- Total Slots
- Available Slots
- Occupied Slots
- Reserved Slots
- Completed Today
- Today's Revenue (₹)

Refreshed on every `VEHICLE_ENTERED`, `VEHICLE_EXITED`, and `DASHBOARD_REFRESH` event, and on manual navigation to the panel.

### VehiclePanel
Two-section layout:
- **Left:** Add vehicle form (registration number, type, owner name, contact)
- **Right/Bottom:** Searchable vehicles table with Edit and Delete actions

Validations are surfaced as status labels below the form, not as blocking pop-ups.

### SlotPanel
Two-section layout:
- **Left:** Add slot form (slot number, type) and a status filter ComboBox
- **Right/Bottom:** Slots table with Edit action; deactivation is guarded

### EntryExitPanel
Three-section layout:
- **Left card:** Vehicle Entry — registration number field + "Enter & Allocate Slot" button. Status label shows allocated slot number.
- **Right card:** Vehicle Exit — registration number field + payment method ComboBox + "Exit & Checkout" button. Status label shows fee.
- **Bottom:** Currently active sessions table (Session ID, Registration, Type, Slot, Entry Time). Refreshed on every entry/exit.

On exit, displays a formatted monospaced receipt in a scrollable dialog.

### ReservationPanel
Two-section layout:
- **Top:** Reservation form (vehicle selection via ComboBox, optional slot selection, start/end DateTime pickers)
- **Bottom:** Reservations table with Cancel action

### TariffPanel
Simple form for updating the three tariff records (Two-Wheeler, Four-Wheeler, Accessible). Each row shows vehicle type, first-hour fee, and additional-hour fee with inline editing.

### ReportPanel
Tabbed layout with tabs for:
- Parking History (date range filter, results table, CSV export button)
- Daily Revenue (date range, table, CSV export)
- Monthly Revenue (table, no date filter)
- Most Used Slots (configurable top-N)

### SettingsPanel
Sections for:
- Change Password (old password, new password, confirm password)
- Allocation Strategy selector (FIRST_AVAILABLE / BEST_FIT)
- Backup operations (Create Backup button, list of existing backup files, Restore button)
- Concurrency test trigger

## 12.3 UI Design Principles

- **Non-blocking:** All database operations run in `SwingWorker.doInBackground()`; results are applied in `done()` on the EDT.
- **Consistent styling:** `UIConstants` defines shared colors, fonts, and dimensions. `UIHelper` provides factory methods for common components.
- **User feedback:** Status labels replace modal pop-ups for routine feedback. `JOptionPane` is reserved for confirmations and receipts.
- **Accessibility:** ComboBoxes for enum selection prevent invalid input. Labels are descriptive.

---

---

# CHAPTER 13: CONCURRENCY AND THREAD SAFETY

## 13.1 Overview

ParkWise has two categories of concurrent code: Swing's Event Dispatch Thread (EDT) model for UI updates, and JDBC transaction management for database safety.

## 13.2 Event Dispatch Thread (EDT)

Java Swing is single-threaded. All Swing component reads and writes must occur on the EDT. ParkWise enforces this in two ways:

1. **SwingUtilities.invokeLater():** Used in `MainFrame`'s Observer callback and in `DashboardRefreshTask` when publishing events that trigger UI updates.

2. **SwingWorker:** Used in `EntryExitPanel` and other panels for all database operations. The `doInBackground()` method runs on a background thread; the `done()` method runs on the EDT.

```java
SwingWorker<ParkingSession, Void> w = new SwingWorker<>() {
    @Override protected ParkingSession doInBackground() throws Exception {
        return allocationService.allocateAndEnter(vehicle);
    }
    @Override protected void done() {
        // Runs on EDT — safe to update Swing components
        ParkingSession s = get();
        entryStatusLabel.setText("Slot: " + s.getSlotNumber());
    }
};
w.execute();
```

## 13.3 Slot Allocation Concurrency

The allocation of a parking slot is the most concurrency-sensitive operation: two simultaneous entry requests must not both be assigned the same slot.

ParkWise uses **two layers** of protection:

**Layer 1 — JVM synchronized block:**
```java
private static final Object ALLOCATION_LOCK = new Object();

synchronized (ALLOCATION_LOCK) {
    // entire allocation logic
}
```
This serializes all allocation attempts within a single JVM instance.

**Layer 2 — Database conditional UPDATE:**
```sql
UPDATE parking_slots
SET slot_status = 'OCCUPIED'
WHERE id = ? AND slot_status = 'AVAILABLE' AND active = 1
```
If two transactions from different JVM instances simultaneously attempt to claim the same slot, the database row lock ensures only one `UPDATE` can succeed. The transaction that finds 0 rows affected immediately rolls back and throws `SlotNotAvailableException`.

This two-layer approach handles both intra-JVM concurrency (multiple Swing workers) and potential cross-JVM scenarios.

## 13.4 DBConnection Thread Safety

`DBConnection.getConnection()` and `releaseConnection()` are both `synchronized` on the instance, ensuring thread-safe access to the connection pool's `ArrayDeque<Connection>`.

`DBConnection.getInstance()` uses double-checked locking with `volatile` to ensure the singleton is constructed safely in a multi-threaded environment.

## 13.5 ParkingEventPublisher Thread Safety

`CopyOnWriteArrayList` is used for the listener collection. On write (subscribe/unsubscribe), it creates a fresh copy of the underlying array — expensive but rare. On read (publish event iteration), it uses a snapshot of the array at the time of iteration, so:
- Listeners added during a publish cycle will not receive the current event
- Listeners removed during a publish cycle will still receive the current event
- No `ConcurrentModificationException` is possible

## 13.6 Background Scheduling

`DashboardRefreshTask` uses `ScheduledExecutorService.scheduleAtFixedRate()` with daemon threads (so the JVM can exit even if the task is running). On application exit, `MainFrame.onExit()` calls `refreshTask.shutdown()` which calls `executor.shutdown()`, cleanly stopping the scheduler.

---

---

# CHAPTER 14: SECURITY IMPLEMENTATION

## 14.1 Password Security

Passwords are stored using salted SHA-256 hashing. The storage format is:
```
<16-byte-hex-salt>:<64-byte-hex-sha256-hash>
```

**Hash generation (`hashPassword`):**
1. Generate 16 cryptographically random bytes using `SecureRandom`
2. Convert to lowercase hex (32-character salt string)
3. Concatenate salt + plaintext: `sha256(salt + password)`
4. Return `salt:hash`

**Hash verification (`verifyPassword`):**
1. Split the stored value on `:` to recover salt and expected hash
2. Compute `sha256(salt + submitted_password)`
3. Compare using constant-time comparison

**Constant-time comparison:**
```java
private static boolean constantTimeEquals(String a, String b) {
    if (a.length() != b.length()) return false;
    int result = 0;
    for (int i = 0; i < a.length(); i++) {
        result |= (Character.compare(a.charAt(i), b.charAt(i)));
    }
    return result == 0;
}
```
The XOR-based comparison runs in exactly `a.length()` time regardless of where the first mismatch occurs, preventing timing side-channel attacks.

## 14.2 SQL Injection Prevention

All DAO methods use `PreparedStatement` exclusively. No SQL is constructed by string concatenation. Parameters are always bound using `ps.setString()`, `ps.setInt()`, `ps.setBigDecimal()`, etc. This eliminates SQL injection entirely at the data layer.

## 14.3 Input Validation

`ValidationUtil` provides regex-based validation for:
- Registration numbers: Indian plate format (e.g., `KA01AB1234`)
- Contact numbers: 10-digit mobile number
- Slot numbers: One letter followed by two digits (e.g., `A01`)

`VehicleService` and `ParkingSlotService` call these validators before any database write, and throw `IllegalArgumentException` with a descriptive message on failure.

## 14.4 Credential Storage

Database credentials (URL, username, password) are stored in `src/main/resources/db.properties` — a file that is listed in `.gitignore` to prevent accidental commit of credentials to version control. The application instructs users explicitly never to commit this file.

---

---

# CHAPTER 15: EXCEPTION HANDLING

## 15.1 Exception Hierarchy

ParkWise defines six domain-specific checked exceptions, all extending `java.lang.Exception`:

| Exception | Use Case |
|---|---|
| `DuplicateVehicleException` | Adding a vehicle with an existing registration |
| `VehicleNotFoundException` | Looking up a registration that does not exist |
| `SlotNotAvailableException` | No free slot of the required type |
| `InvalidParkingSessionException` | Session not found, wrong state, or clock issue |
| `InvalidTariffException` | Tariff with negative fee or missing for vehicle type |
| `ReservationOverlapException` | New reservation overlaps an existing confirmed one |

## 15.2 Exception Handling Strategy

Checked exceptions are thrown by service methods and caught in UI panels. The UI displays the exception message as a status label (green for success, red for error) without crashing or showing raw stack traces.

For SQL exceptions, the pattern is:
```java
try {
    conn.setAutoCommit(false);
    // ... multiple DB operations ...
    conn.commit();
} catch (SQLException e) {
    if (conn != null) conn.rollback();
    throw e;   // re-throw for the service caller to handle
} finally {
    if (conn != null) conn.setAutoCommit(true);
    DBConnection.getInstance().releaseConnection(conn);
}
```

The `finally` block guarantees that connections are always released and autocommit is always restored, preventing connection leaks.

## 15.3 Startup Error Handling

If `DatabaseInitializer.initialize()` fails at startup (e.g., MySQL not running, wrong credentials), `Main.java` catches the exception, displays a detailed `JOptionPane` error dialog with diagnostic hints, and exits with code 1. The application never enters an inconsistent state.

---

---

# CHAPTER 16: TESTING

## 16.1 Testing Philosophy

The test suite covers all non-database-dependent components. Tests run without a MySQL connection and are designed to be deterministic and reproducible on any machine with a JDK.

Database-dependent integration tests (like `DBConnectionSmokeTest`) are present but skipped in standard CI since they require a live database.

## 16.2 Test Files

| Test Class | Location | What It Tests |
|---|---|---|
| `FeeCalculationTest` | `strategy/` | StandardFeeStrategy: boundary values, ceiling division, zero duration |
| `SlotAllocationStrategyTest` | `strategy/` | FirstAvailableStrategy and BestFitStrategy: empty list, single slot, multiple slots |
| `ConcurrentAllocationTest` | `strategy/` | Thread-safety of slot claiming: 10 threads attempting simultaneous allocation |
| `PasswordUtilTest` | `util/` | Hash/verify round-trip, wrong password rejection, null input handling |
| `ValidationUtilTest` | `util/` | Valid/invalid registration numbers, contact numbers, slot numbers |
| `BackupServiceTest` | `backup/` | Serialization round-trip: serialize then deserialize and verify data integrity |
| `DBConnectionSmokeTest` | root | Connection pool smoke test (requires live DB, skipped in standard CI) |

## 16.3 Key Test Cases

**FeeCalculationTest (StandardFeeStrategy):**
- 0 minutes → ₹20 (minimum charge of first hour)
- 45 minutes → ₹20 (within first hour)
- 60 minutes → ₹20 (exactly one hour)
- 61 minutes → ₹30 (one additional started hour)
- 120 minutes → ₹30 (two full hours)
- 121 minutes → ₹40 (three hours charged)
- Negative input → `IllegalArgumentException`

**PasswordUtilTest:**
- `hashPassword("Admin@123")` → returns non-null string in `salt:hash` format
- `verifyPassword("Admin@123", hash)` → true
- `verifyPassword("wrong", hash)` → false
- `verifyPassword("Admin@123", null)` → false (no NPE)
- Two calls to `hashPassword()` with the same input produce different hashes (salt randomness)

**ConcurrentAllocationTest:**
- Launches 10 threads simultaneously attempting to claim one of 5 available slots
- Verifies exactly 5 succeed (slots fully allocated)
- Verifies exactly 5 fail with `SlotNotAvailableException`
- Verifies no slot is claimed more than once

## 16.4 Running Tests

```bash
mvn test
```

Tests that do NOT require a database:
- `FeeCalculationTest`
- `SlotAllocationStrategyTest`
- `ConcurrentAllocationTest`
- `PasswordUtilTest`
- `ValidationUtilTest`
- `BackupServiceTest`

---

---

# CHAPTER 17: FEE CALCULATION LOGIC

## 17.1 Algorithm

The `StandardFeeStrategy.calculate(int durationMinutes, Tariff tariff)` method implements the following algorithm:

1. If `durationMinutes < 0`, throw `IllegalArgumentException`
2. Set `effectiveMinutes = max(durationMinutes, 1)` (minimum billing of 1 minute)
3. Start with `fee = tariff.getFirstHourFee()`
4. If `effectiveMinutes > 60`:
   - `additionalMinutes = effectiveMinutes - 60`
   - `additionalHours = ceil(additionalMinutes / 60)` = `(additionalMinutes + 59) / 60`
   - `additionalFee = tariff.getAdditionalHourFee() × additionalHours`
   - `fee = fee + additionalFee`
5. Return `fee`

## 17.2 Calculation Examples (Four-Wheeler: ₹40 first hour, ₹20 per additional)

| Duration | Additional Minutes | Additional Hours (ceil) | Fee |
|---|---|---|---|
| 30 min | 0 | 0 | ₹40 |
| 60 min | 0 | 0 | ₹40 |
| 61 min | 1 | 1 | ₹60 |
| 90 min | 30 | 1 | ₹60 |
| 120 min | 60 | 1 | ₹60 |
| 121 min | 61 | 2 | ₹80 |
| 180 min | 120 | 2 | ₹80 |
| 181 min | 121 | 3 | ₹100 |

## 17.3 Calculation Example (Accessible: ₹20 first hour, ₹10 per additional)

From the sample receipt: Session KA05IJ7890, Entry 12:00, Exit 14:30 = 150 minutes
- First hour: ₹20
- Additional minutes: 90
- Additional hours: ceil(90/60) = 2
- Additional fee: 2 × ₹10 = ₹20
- **Total: ₹40.00** ✓ (matches sample receipt)

## 17.4 Monetary Precision

All fee values are `java.math.BigDecimal`. The `Tariff` table uses `DECIMAL(10,2)`. `BigDecimal.multiply()` and `BigDecimal.add()` are used for all arithmetic. This eliminates the well-known floating-point errors in Java `double` arithmetic (e.g., `0.1 + 0.2 = 0.30000000000000004` in double, but exact in BigDecimal).

---

---

# CHAPTER 18: BACKUP AND REPORT FEATURES

## 18.1 Serialization Backup

Java object serialization allows the state of a Java object graph to be converted into a byte stream and reconstituted later.

`BackupSnapshot` is the root serializable object. It contains:
- `List<Vehicle> vehicles`
- `List<ParkingSlot> slots`
- `LocalDateTime createdAt`
- `int vehicleCount`, `int slotCount`

All contained objects (`Vehicle`, `ParkingSlot`) implement `Serializable` with explicit `serialVersionUID = 1L`.

**Write path (ObjectOutputStream):**
```java
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
    oos.writeObject(snapshot);
}
```

**Read path (ObjectInputStream):**
```java
try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
    BackupSnapshot snapshot = (BackupSnapshot) ois.readObject();
}
```

Backup files are named `parkwise_backup_yyyyMMdd_HHmmss.bak` and stored in the configured `AppConfig.BACKUP_DIR`.

## 18.2 CSV Report Export

CSV export uses Java character I/O streams:

```java
try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
    bw.write("Session ID,Registration,...");
    bw.newLine();
    for (ParkingSession s : sessions) {
        bw.write(String.join(",", ...));
        bw.newLine();
    }
}
```

`BufferedWriter` wraps `FileWriter` to buffer writes, reducing the number of actual file system calls. The CSV escaping method wraps fields in double quotes if they contain commas, quotes, or newlines, following the RFC 4180 standard.

## 18.3 Stream API in Reports

`ReportService` demonstrates extensive Stream API usage:

- `filter()` — exclude null values and non-PAID sessions
- `sorted()` — sort by entry time descending
- `groupingBy()` — aggregate by date or month
- `reducing()` — sum BigDecimal values
- `counting()` — count occurrences per slot
- `mapToInt()` + `average()` — compute average duration as OptionalDouble
- `limit()` — get top-N most-used slots
- `TreeMap::new` as the supplier — keep dates sorted automatically

---

---

# CHAPTER 19: DATA FLOW DIAGRAMS

## 19.1 Vehicle Entry Flow

```
User enters registration number
        ↓
EntryExitPanel.doEntry()
        ↓ (SwingWorker.doInBackground)
VehicleService.findByRegistrationNumber()
        ↓
VehicleDAO.findByRegistrationNumber() → MySQL: SELECT * FROM vehicles WHERE...
        ↓
Vehicle object returned
        ↓
AllocationService.allocateAndEnter(vehicle)
        ├── synchronized(ALLOCATION_LOCK)
        ├── sessionDAO.findActiveByVehicleId() → no active session
        ├── conn = DBConnection.getConnection()
        ├── conn.setAutoCommit(false)
        ├── slotDAO.findAvailableByType(vehicleType) → List<ParkingSlot>
        ├── strategy.selectSlot(eligibleSlots) → ParkingSlot selected
        ├── UPDATE parking_slots ... WHERE id=? AND slot_status='AVAILABLE'
        │   └── 0 rows? → rollback → SlotNotAvailableException
        ├── sessionDAO.insert(conn, session) → new session ID
        ├── conn.commit()
        └── ParkingEventPublisher.publish(VEHICLE_ENTERED)
                ↓
        MainFrame Observer → SwingUtilities.invokeLater → dashboardPanel.refresh()
        ↓ (SwingWorker.done)
EntryExitPanel shows "Allocated slot: A03 | Session ID: 12"
```

## 19.2 Vehicle Exit Flow

```
User enters registration number + selects payment method
        ↓
EntryExitPanel.doExit()
        ↓ (SwingWorker.doInBackground)
VehicleService.findByRegistrationNumber() → Vehicle
        ↓
ParkingService.getActiveSession(vehicleId) → ParkingSession
        ↓
ParkingService.processExit(sessionId, paymentMethod)
        ├── sessionDAO.findById(sessionId) → verify ACTIVE
        ├── exitTime = LocalDateTime.now()
        ├── Duration.between(entryTime, exitTime).toMinutes()
        ├── TariffService.getTariffForType(vehicleType) → Tariff
        ├── StandardFeeStrategy.calculate(minutes, tariff) → BigDecimal fee
        ├── conn.setAutoCommit(false)
        ├── sessionDAO.completeSession(conn, ...) → UPDATE parking_sessions
        ├── paymentDAO.insert(conn, payment) → INSERT payments
        ├── slotDAO.updateStatus(conn, slotId, AVAILABLE) → UPDATE parking_slots
        ├── conn.commit()
        └── ParkingEventPublisher.publish(VEHICLE_EXITED)
                ↓
        MainFrame Observer → dashboardPanel.refresh()
        ↓ (SwingWorker.done)
EntryExitPanel.showReceipt(session) → JOptionPane with formatted receipt
```

## 19.3 Dashboard Refresh Flow

```
Event published (VEHICLE_ENTERED / VEHICLE_EXITED / DASHBOARD_REFRESH)
        ↓
ParkingEventPublisher.publish(event)
        ↓
MainFrame listener.onEvent(event)
        ↓
SwingUtilities.invokeLater(() -> dashboardPanel.refresh())
        ↓
DashboardPanel.refresh()
        ↓ (SwingWorker.doInBackground)
ParkingSlotService.getDashboardCounts() → [total, avail, occupied, reserved]
ParkingService.getCompletedTodayCount() → int
ParkingService.getTodayRevenue() → BigDecimal
        ↓ (SwingWorker.done — on EDT)
Update all JLabel components with new values
```

---

---

# CHAPTER 20: RESULTS AND FEATURES SUMMARY

## 20.1 Functional Features Delivered

| Feature | Status | Notes |
|---|---|---|
| Admin Login | Complete | Salted SHA-256, constant-time comparison |
| Vehicle Registration | Complete | Regex validation, duplicate check, cache |
| Vehicle Search | Complete | DB search + Stream deduplication |
| Slot Management | Complete | Add/edit, deactivation guard |
| Smart Slot Allocation | Complete | Two strategies, two-layer concurrency safety |
| Vehicle Entry | Complete | Atomic JDBC transaction |
| Vehicle Exit + Fee | Complete | BigDecimal arithmetic, receipt dialog |
| Reservations | Complete | Overlap detection within transaction |
| Tariff Configuration | Complete | Per-type rates, BigDecimal validation |
| Live Dashboard | Complete | Observer + ScheduledExecutorService |
| Parking History Report | Complete | Date range, Stream-sorted |
| Daily Revenue Report | Complete | groupingBy + reducing |
| Monthly Revenue Report | Complete | TreeMap sorted by month |
| Most Used Slots | Complete | groupingBy + counting + limit |
| Average Duration | Complete | mapToInt + average |
| CSV Export | Complete | BufferedWriter, RFC 4180 escaping |
| Serialization Backup | Complete | ObjectOutputStream/ObjectInputStream |
| Password Change | Complete | Old password verification, re-hash |
| Strategy Switching | Complete | Runtime AppConfig, no code change needed |
| Concurrency Test | Complete | In SettingsPanel, spawns N threads |
| Unit Tests | Complete | 6 test classes, no DB dependency |

## 20.2 Java Topics Demonstrated

| Java Topic | Where Used |
|---|---|
| Classes and Objects | Every model and service class |
| Encapsulation | Private fields + getters/setters in all models |
| Inheritance | Exception hierarchy extends Exception |
| Interfaces | SlotAllocationStrategy, ParkingFeeStrategy, NotificationService, ParkingEventListener |
| Enum | VehicleType, SlotStatus, SessionStatus, PaymentMethod, EventType |
| Generics | List<Vehicle>, Map<String,Vehicle>, Optional<T> |
| Collections (ArrayList) | Service return types |
| Collections (HashMap) | VehicleService cache |
| Collections (HashSet) | Deduplication in VehicleService.search() |
| Collections (TreeMap) | Revenue reports sorted by date |
| Collections (ArrayDeque) | DBConnection pool |
| Collections (CopyOnWriteArrayList) | ParkingEventPublisher listeners |
| Stream API | ReportService, VehicleService, ParkingSlotService |
| Lambda Expressions | forEach, filter, sorted, collect throughout |
| Method References | AllocationService, ReportService |
| Optional | findById(), findByRegistrationNumber() |
| java.time | LocalDateTime, Duration, LocalDate throughout |
| BigDecimal | Monetary values throughout |
| Wrapper Classes | Integer/Character in PasswordUtil, Math.max |
| Autoboxing/Unboxing | int ↔ Integer in multiple locations |
| Serialization | BackupService, BackupSnapshot, Vehicle, ParkingSlot |
| I/O Streams (Byte) | ObjectOutputStream/ObjectInputStream in BackupService |
| I/O Streams (Character) | BufferedWriter/FileWriter in ReportService |
| Multithreading | SwingWorker, ScheduledExecutorService, synchronized |
| Singleton | DBConnection, AppConfig, ParkingEventPublisher |
| Factory Pattern | NotificationFactory |
| Strategy Pattern | SlotAllocationStrategy, ParkingFeeStrategy |
| Observer Pattern | ParkingEventPublisher/ParkingEventListener |
| JDBC | All DAO classes |
| PreparedStatement | All DAO SQL queries |
| Transactions | AllocationService, ParkingService, ReservationService |
| Custom Exceptions | 6 domain-specific checked exceptions |
| Try-with-resources | All stream and DB resource management |
| Switch Expression | NotificationFactory, AllocationService.resolveStrategy() |
| Text Block | EntryExitPanel receipt generation |
| Pattern: Cloneable | ParkingSlot copy constructor and clone() |

---

---

# CHAPTER 21: FUTURE ENHANCEMENTS

1. **ANPR Integration:** Integrate with an Automatic Number Plate Recognition camera API to automate vehicle registration number entry, eliminating manual input errors.

2. **Mobile Companion App:** A React Native or Flutter app for vehicle owners to check real-time slot availability, make reservations, and receive push notifications when their reservation is about to start.

3. **Web Interface:** A Spring Boot + Thymeleaf web frontend allowing remote management without installing the desktop app.

4. **Email/SMS Notifications:** Integrate with Twilio (SMS) or SendGrid (email) via the existing NotificationFactory to alert vehicle owners of reservation reminders, payment receipts, and overstay warnings.

5. **Advanced Tariff Rules:** Support time-of-day pricing (peak/off-peak), day-of-week differentials, monthly season passes, and first-N-minutes-free grace periods.

6. **Multi-Level Parking:** Extend the slot model with a `floor` attribute and add floor-selection logic to the allocation strategies.

7. **bcrypt/Argon2 Passwords:** Replace SHA-256 with a proper password hashing function (bcrypt is available via org.mindrot:jbcrypt or as part of Spring Security's crypto module) for stronger brute-force resistance.

8. **HikariCP Connection Pool:** Replace the hand-written pool with HikariCP for production-grade performance, health checking, and metrics.

9. **Audit Log:** Add an `audit_log` table recording every admin action (login, vehicle add, slot change, tariff update) with timestamp and admin ID for compliance and forensics.

10. **Overstay Detection:** Add a background task that identifies vehicles whose duration has exceeded a configurable threshold and alerts the admin.

11. **JavaFX Migration:** Migrate the UI from Swing to JavaFX for a modern look-and-feel, CSS-based styling, and better support for animated transitions and responsive layouts.

12. **Internationalization (i18n):** Externalize all UI strings to resource bundles to support multiple languages.

---

---

# CHAPTER 22: CONCLUSION

ParkWise demonstrates that a well-designed, professional-grade application can be built entirely with Core Java and the JDK standard library, without depending on any application framework.

The project successfully achieves all primary objectives:
- A fully functional parking management system with 9 distinct functional modules
- A clean, strictly enforced four-layer architecture (UI → Service → DAO → Database)
- Comprehensive coverage of Core Java syllabus topics from basic OOP through advanced concurrency and I/O
- Four design patterns (Singleton, Factory, Strategy, Observer) applied in genuine, non-trivial contexts
- ACID-compliant multi-step database operations
- Two-layer concurrency protection preventing double-allocation under race conditions
- Salted SHA-256 password security with constant-time comparison
- A suite of unit tests validating critical algorithms without database dependencies

The codebase contains approximately 4,500 lines of production Java code across 50+ source files, organized into 12 packages. Every design decision is intentional and documented in code comments.

ParkWise stands as a comprehensive demonstration of object-oriented design, Java programming maturity, and the ability to deliver a complete software system from database schema through business logic to user interface — skills that form the foundation of professional software engineering.

---

---

# CHAPTER 23: REFERENCES

1. Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software.* Addison-Wesley.

2. Bloch, J. (2018). *Effective Java* (3rd ed.). Addison-Wesley.

3. Oracle Corporation. (2024). *Java SE 17 API Specification.* https://docs.oracle.com/en/java/docs/

4. Oracle Corporation. (2024). *JDBC API Specification.* https://docs.oracle.com/javase/tutorial/jdbc/

5. Oracle Corporation. (2024). *Java Concurrency in Practice — Java SE Documentation.* https://docs.oracle.com/javase/tutorial/essential/concurrency/

6. MySQL AB. (2024). *MySQL 8.0 Reference Manual.* https://dev.mysql.com/doc/refman/8.0/en/

7. Apache Software Foundation. (2024). *Apache Maven Documentation.* https://maven.apache.org/guides/

8. JUnit Team. (2024). *JUnit 5 User Guide.* https://junit.org/junit5/docs/current/user-guide/

9. Jenkov, J. (2024). *Java Swing Tutorial.* https://jenkov.com/tutorials/java-swing/

10. Subramaniam, V. (2014). *Functional Programming in Java.* Pragmatic Bookshelf.

---

---

# CHAPTER 24: APPENDIX — COMPLETE SOURCE FILE INDEX

## A. Main Entry Point

| File | Package | Description |
|---|---|---|
| Main.java | com.parkwise | Application entry point; startup sequence |

## B. Model Layer (9 files)

| File | Package | Description |
|---|---|---|
| Admin.java | com.parkwise.model | Administrator account |
| Vehicle.java | com.parkwise.model | Registered vehicle |
| ParkingSlot.java | com.parkwise.model | Physical parking slot (Serializable, Cloneable) |
| ParkingSession.java | com.parkwise.model | Vehicle entry/exit session |
| Tariff.java | com.parkwise.model | Fee configuration per vehicle type |
| Reservation.java | com.parkwise.model | Advance parking booking |
| Payment.java | com.parkwise.model | Payment transaction record |
| SlotStatus.java | com.parkwise.model | Enum: AVAILABLE/OCCUPIED/RESERVED/MAINTENANCE |
| VehicleType.java | com.parkwise.model | Enum: TWO_WHEELER/FOUR_WHEELER/ACCESSIBLE |

## C. DAO Layer (7 files)

| File | Package | Description |
|---|---|---|
| AdminDAO.java | com.parkwise.dao | Admin CRUD |
| VehicleDAO.java | com.parkwise.dao | Vehicle CRUD + search |
| ParkingSlotDAO.java | com.parkwise.dao | Slot CRUD + availability queries |
| ParkingSessionDAO.java | com.parkwise.dao | Session CRUD + statistics queries |
| ReservationDAO.java | com.parkwise.dao | Reservation CRUD + overlap detection |
| TariffDAO.java | com.parkwise.dao | Tariff read and update |
| PaymentDAO.java | com.parkwise.dao | Payment insert and read |

## D. Service Layer (7 files)

| File | Package | Description |
|---|---|---|
| AuthService.java | com.parkwise.service | Admin authentication and password management |
| VehicleService.java | com.parkwise.service | Vehicle business logic + HashMap cache |
| ParkingSlotService.java | com.parkwise.service | Slot management + Stream filtering |
| AllocationService.java | com.parkwise.service | Entry + slot allocation (concurrency critical) |
| ParkingService.java | com.parkwise.service | Exit + fee calculation + payment |
| ReservationService.java | com.parkwise.service | Reservation lifecycle |
| TariffService.java | com.parkwise.service | Tariff management |

## E. Strategy Layer (5 files)

| File | Package | Description |
|---|---|---|
| SlotAllocationStrategy.java | com.parkwise.strategy | Interface for slot selection |
| FirstAvailableStrategy.java | com.parkwise.strategy | Select smallest slot number |
| BestFitStrategy.java | com.parkwise.strategy | Select most type-compatible slot |
| ParkingFeeStrategy.java | com.parkwise.strategy | Interface for fee calculation |
| StandardFeeStrategy.java | com.parkwise.strategy | First-hour + ceil-division additional hours |

## F. Observer Layer (3 files)

| File | Package | Description |
|---|---|---|
| ParkingEventPublisher.java | com.parkwise.observer | Singleton event bus (CopyOnWriteArrayList) |
| ParkingEventListener.java | com.parkwise.observer | Interface: onEvent(ParkingEvent) |
| ParkingEvent.java | com.parkwise.observer | Event carrier: type, message, payload |

## G. Factory Layer (4 files)

| File | Package | Description |
|---|---|---|
| NotificationFactory.java | com.parkwise.factory | Creates NotificationService by channel name |
| NotificationService.java | com.parkwise.factory | Interface: notifyInfo(), notifyError() |
| DialogNotificationService.java | com.parkwise.factory | Swing JOptionPane implementation |
| LogNotificationService.java | com.parkwise.factory | java.util.logging implementation |

## H. Exception Layer (6 files)

| File | Package | Description |
|---|---|---|
| DuplicateVehicleException.java | com.parkwise.exception | Duplicate registration |
| VehicleNotFoundException.java | com.parkwise.exception | Vehicle not found |
| SlotNotAvailableException.java | com.parkwise.exception | No free slot |
| InvalidParkingSessionException.java | com.parkwise.exception | Bad session state |
| InvalidTariffException.java | com.parkwise.exception | Bad tariff values |
| ReservationOverlapException.java | com.parkwise.exception | Overlapping reservation |

## I. Utility Layer (7 files)

| File | Package | Description |
|---|---|---|
| DBConnection.java | com.parkwise.util | Singleton connection pool |
| AppConfig.java | com.parkwise.util | Singleton configuration store |
| PasswordUtil.java | com.parkwise.util | Salted SHA-256 hashing |
| ValidationUtil.java | com.parkwise.util | Input validation (regex) |
| DatabaseInitializer.java | com.parkwise.util | Idempotent startup DB setup |
| DashboardRefreshTask.java | com.parkwise.util | ScheduledExecutorService wrapper |
| ConcurrentAllocationSimulator.java | com.parkwise.util | Concurrency test simulator |

## J. Report Layer (1 file)

| File | Package | Description |
|---|---|---|
| ReportService.java | com.parkwise.report | Report generation + CSV export |

## K. Backup Layer (2 files)

| File | Package | Description |
|---|---|---|
| BackupService.java | com.parkwise.backup | Serialize/deserialize backup |
| BackupSnapshot.java | com.parkwise.backup | Root serializable snapshot object |

## L. UI Layer (12 files)

| File | Package | Description |
|---|---|---|
| LoginScreen.java | com.parkwise.ui | Admin login screen |
| MainFrame.java | com.parkwise.ui | Main window, sidebar, CardLayout |
| DashboardPanel.java | com.parkwise.ui | Live statistics dashboard |
| VehiclePanel.java | com.parkwise.ui | Vehicle management screen |
| SlotPanel.java | com.parkwise.ui | Slot management screen |
| EntryExitPanel.java | com.parkwise.ui | Vehicle entry/exit + receipt |
| ReservationPanel.java | com.parkwise.ui | Reservation management screen |
| TariffPanel.java | com.parkwise.ui | Tariff configuration screen |
| ReportPanel.java | com.parkwise.ui | Reports and CSV export screen |
| SettingsPanel.java | com.parkwise.ui | Password, strategy, backup screen |
| UIConstants.java | com.parkwise.ui | Colors, fonts, dimensions |
| UIHelper.java | com.parkwise.ui | Component factory methods |

## M. Test Layer (7 files)

| File | Package | Description |
|---|---|---|
| FeeCalculationTest.java | strategy/ | Fee boundary value testing |
| SlotAllocationStrategyTest.java | strategy/ | Strategy behavior testing |
| ConcurrentAllocationTest.java | strategy/ | Thread-safety testing |
| PasswordUtilTest.java | util/ | Hash round-trip testing |
| ValidationUtilTest.java | util/ | Input validation testing |
| BackupServiceTest.java | backup/ | Serialization round-trip testing |
| DBConnectionSmokeTest.java | root | Connection pool smoke test |

## N. Configuration and Build Files

| File | Description |
|---|---|
| pom.xml | Maven build configuration |
| src/main/resources/db.properties | Database connection configuration (gitignored) |
| sql/schema.sql | Database DDL |
| sql/sample_data.sql | Seed data |
| samples/sample_parking_receipt.txt | Example receipt output |
| samples/sample_parking_report.csv | Example CSV export |
| Run_ParkWise.bat | Windows batch launcher |
| ARCHITECTURE.md | Architecture explanation |
| README.md | Setup and usage guide |

---

*End of Project Report*

---

**Total Source Files:** 57 Java files (50 production + 7 test)  
**Packages:** 12  
**Design Patterns:** 4 (Singleton, Factory, Strategy, Observer)  
**Database Tables:** 7  
**UI Screens:** 10  
**Custom Exceptions:** 6  
**Lines of Code (approx.):** 4,500+ production Java  

---
