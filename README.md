# ParkWise — Intelligent Parking Allocation System

A fully functional Java desktop application for managing parking slots, vehicle entry/exit, fee calculation, reservations, and analytics. Built as a CSE college project demonstrating Core Java, OOP, Collections, JDBC, Multithreading, and Design Patterns.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI | Java Swing |
| Database | MySQL 8.x |
| DB Driver | MySQL Connector/J 8.3.0 |
| Build | Maven |
| Testing | JUnit 5 |

No Spring, Hibernate, React, or any other framework.

---

## Prerequisites

1. **Java 17+** — `java -version` must show 17 or later.
2. **Maven 3.8+** — `mvn -version`
3. **MySQL 8.0+** running locally or remotely.

---

## Database Setup

### Step 1 — Create the database and schema

Open MySQL Workbench or the MySQL CLI and run:

```sql
SOURCE /path/to/ParkWise/sql/schema.sql;
```

### Step 2 — Load sample data (optional but recommended)

```sql
SOURCE /path/to/ParkWise/sql/sample_data.sql;
```

### Step 3 — Configure the connection

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/parkwise_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

**Never commit real credentials to version control.**

---

## Build and Run

```bash
# From the project root (where pom.xml lives)
mvn clean package -DskipTests

# Run the application
java -jar target/parkwise.jar
```

To run with tests:

```bash
mvn clean verify
```

---

## First Login

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `Admin@123` |

**Change this password immediately after the first login** via Settings → Change Password.

---

## Application Modules

| Screen | Description |
|---|---|
| Login | Secure admin authentication with salted SHA-256 hashing |
| Dashboard | Live stats: total/available/occupied/reserved slots, revenue |
| Vehicle Management | Add, search, edit, and delete registered vehicles |
| Slot Management | Add/edit slots, filter by status, deactivation guards |
| Entry / Exit | Intelligent allocation, fee calculation, receipt generation |
| Reservations | Advance bookings with overlap prevention |
| Tariff Configuration | Configurable per-type fee rates |
| Reports & Analytics | Parking history, daily/monthly revenue, CSV export |
| Settings & Backup | Strategy selection, serialized backup, concurrency test |

---

## Design Patterns Implemented

| Pattern | Location | Purpose |
|---|---|---|
| **Singleton** | `DBConnection`, `AppConfig`, `ParkingEventPublisher` | Shared resources with single access point |
| **Factory** | `NotificationFactory` | Creates notification implementations |
| **Strategy** | `SlotAllocationStrategy`, `ParkingFeeStrategy` | Swappable slot selection and fee algorithms |
| **Observer** | `ParkingEventPublisher` / `ParkingEventListener` | Event-driven dashboard updates and notifications |

---

## Allocation Strategies

| Strategy | Behaviour |
|---|---|
| **FIRST_AVAILABLE** | Assigns the slot with the lexicographically smallest slot number |
| **BEST_FIT** | Selects the most type-compatible slot with consistent tie-breaking |

Switch between them in the Settings screen.

---

## Concurrency Safety

Vehicle entry/slot allocation uses two layers of protection:

1. **JVM `synchronized` block** — prevents two threads in the same JVM from entering the critical section simultaneously.
2. **Database conditional UPDATE** — `UPDATE parking_slots SET slot_status='OCCUPIED' WHERE id=? AND slot_status='AVAILABLE'` ensures that even across distributed JVMs, only one transaction can claim a slot.

A failed conditional update triggers an immediate rollback and a `SlotNotAvailableException`.

---

## Fee Calculation Rules

| Duration | Charge |
|---|---|
| 0–60 min | First-hour fee |
| 61–120 min | First-hour fee + 1 × additional-hour fee |
| 121–180 min | First-hour fee + 2 × additional-hour fee |
| … | … |

Any started additional hour is charged as a complete hour.

Default demo tariffs:
- Two-wheeler: ₹20 first hour, ₹10 per additional started hour
- Four-wheeler: ₹40 first hour, ₹20 per additional started hour
- Accessible: ₹20 / ₹10 (same as two-wheeler by default)

---

## Backup Note

The serialized backup (Settings → Create Backup) is a **syllabus demonstration** of Java object serialization. It is NOT a replacement for a proper MySQL backup. For production use:

```bash
mysqldump -u root -p parkwise_db > parkwise_backup.sql
```

---

## Project Structure

```
src/
  main/
    java/com/parkwise/
      model/          Entity classes (Vehicle, ParkingSlot, ParkingSession, …)
      dao/            JDBC data-access objects
      service/        Business logic and transactions
      strategy/       Slot allocation and fee calculation strategies
      factory/        Notification service factory
      observer/       Event publisher and listener interface
      exception/      Custom checked exceptions
      util/           DBConnection (Singleton), validation, config, threading
      report/         Report generation and CSV export
      backup/         Serialization backup service
      ui/             Swing screens and components
      Main.java       Application entry point
  test/
    java/com/parkwise/
      strategy/       Fee and allocation strategy tests
      util/           Password and validation tests
      backup/         Serialization round-trip tests
sql/
  schema.sql          Database DDL
  sample_data.sql     Seed data for development/testing
```

---

## Running Tests

```bash
mvn test
```

Tests that do NOT require a database connection:
- `FeeCalculationTest` — fee calculation correctness
- `SlotAllocationStrategyTest` — strategy behaviour
- `ConcurrentAllocationTest` — thread-safety of slot claiming
- `PasswordUtilTest` — hash/verify round-trip
- `ValidationUtilTest` — input validation
- `BackupServiceTest` — serialization round-trip
