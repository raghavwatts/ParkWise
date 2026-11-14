# ParkWise — Viva Preparation Guide

---

## 1. Project Overview

**Q: What is ParkWise?**
ParkWise is an intelligent parking allocation system built as a Java Swing desktop application. It manages parking slots, registers vehicles, allocates slots automatically on vehicle arrival, calculates fees, handles reservations, and generates reports. It connects to a MySQL database via JDBC.

**Q: What technologies did you use?**
Java 17, Java Swing for the GUI, JDBC for database operations, MySQL 8 as the database, and Maven for dependency management. The only external library is the MySQL Connector/J JDBC driver.

---

## 2. Object-Oriented Programming

**Q: How is encapsulation demonstrated?**
All model classes (`Vehicle`, `ParkingSlot`, `ParkingSession`, etc.) declare fields `private` and expose them only through `public` getters and setters. Business logic is kept in service classes, not exposed directly from models.

**Q: Where is inheritance used?**
All custom exceptions (`SlotNotAvailableException`, `VehicleNotFoundException`, etc.) extend `Exception`. The `ParkingSession` and `Payment` classes contain nested enums that are logically part of their parent type.

**Q: How is polymorphism demonstrated?**
- Runtime: `SlotAllocationStrategy` and `ParkingFeeStrategy` are interfaces; `FirstAvailableStrategy`, `BestFitStrategy`, and `StandardFeeStrategy` override their methods. The service calls `strategy.selectSlot(...)` without knowing the concrete type.
- Compile-time: Method overloading is shown in `ParkingEvent` constructors (two-arg and three-arg variants).

**Q: What is abstraction?**
`SlotAllocationStrategy` and `ParkingFeeStrategy` are interfaces defining what a strategy does without specifying how. `NotificationService` is an interface separating the notification contract from its implementations.

---

## 3. Design Patterns

**Q: Explain the Singleton pattern.**
`DBConnection`, `AppConfig`, and `ParkingEventPublisher` use double-checked locking with a `volatile` field. This ensures only one instance is ever created, even in a multi-threaded environment, while avoiding the performance cost of fully synchronized access on every call.

**Q: Explain the Strategy pattern.**
The `SlotAllocationStrategy` interface defines `selectSlot(List<ParkingSlot>)`. `AllocationService` calls `resolveStrategy()` at runtime based on `AppConfig`, which returns either `FirstAvailableStrategy` or `BestFitStrategy`. Swapping the strategy requires no change to the service code.

**Q: Explain the Observer pattern.**
`ParkingEventPublisher` (the subject) maintains a `CopyOnWriteArrayList` of `ParkingEventListener` instances. When `AllocationService` or `ParkingService` completes an operation, they call `publisher.publish(event)`, which iterates all listeners. The dashboard subscribes and refreshes itself on each `VEHICLE_ENTERED` or `VEHICLE_EXITED` event.

**Q: Explain the Factory pattern.**
`NotificationFactory.create("DIALOG")` returns a `DialogNotificationService`; `create("LOG")` returns a `LogNotificationService`. The client asks for a notification by channel name and receives the appropriate implementation without knowing its constructor.

---

## 4. Collections Framework

**Q: Which collections do you use and why?**
- `ArrayList` — ordered result sets from DAO queries and report data.
- `HashMap` — in-memory vehicle lookup cache in `VehicleService` (O(1) access by registration number).
- `TreeMap` — daily and monthly revenue reports (keys are dates/month strings; TreeMap keeps them sorted).
- `HashSet` — duplicate detection when filtering vehicles by registration number in `VehicleService.search()`.
- `CopyOnWriteArrayList` — thread-safe listener list in `ParkingEventPublisher`.
- `ArrayDeque` — connection pool in `DBConnection` (stack behaviour for LIFO reuse).

---

## 5. Exception Handling

**Q: What custom exceptions did you create?**
`SlotNotAvailableException`, `VehicleNotFoundException`, `InvalidParkingSessionException`, `InvalidTariffException`, `DuplicateVehicleException`, `ReservationOverlapException`. All are checked exceptions so the compiler enforces handling.

**Q: Why checked vs unchecked?**
Business rule violations (no slot available, duplicate vehicle) are checked because the caller must handle them meaningfully. Programming errors like null pointer dereferences remain unchecked.

**Q: How do transactions relate to exception handling?**
`AllocationService` and `ParkingService` set `autoCommit=false`, do their work, and call `commit()`. Any exception triggers a `rollback()` in the `catch` block, and the connection is always returned to the pool in the `finally` block.

---

## 6. Multithreading

**Q: How did you prevent double-allocation?**
Two layers: (1) A `synchronized(ALLOCATION_LOCK)` block in `AllocationService` prevents two JVM threads from executing the critical section concurrently. (2) A conditional SQL `UPDATE ... WHERE slot_status='AVAILABLE'` ensures that even if two separate applications connect to the same DB, only one UPDATE affects a row; the other sees 0 rows updated and rolls back.

**Q: What is SwingWorker and why do you use it?**
Swing is single-threaded; all UI updates must happen on the Event Dispatch Thread (EDT). `SwingWorker` runs database calls in `doInBackground()` (a separate thread) and delivers results back to the EDT in `done()`. Without this, long DB queries would freeze the UI.

**Q: What is ScheduledExecutorService used for?**
`DashboardRefreshTask` uses a `ScheduledExecutorService` to publish a `DASHBOARD_REFRESH` event every 30 seconds. The dashboard's observer receives this event on a background thread and dispatches the UI update via `SwingUtilities.invokeLater()`.

---

## 7. Stream API and Lambda Expressions

**Q: Give examples of Stream API usage.**
- `VehicleService.search()` — filters and collects distinct vehicles: `stream().filter(...).collect(Collectors.toCollection(...))`.
- `ReportService.getDailyRevenue()` — groups sessions by date and sums fees: `stream().collect(Collectors.groupingBy(..., TreeMap::new, Collectors.reducing(...)))`.
- `ReportService.getMostUsedSlots()` — groups, counts, sorts, and limits: `stream().collect(groupingBy(..., counting())).entrySet().stream().sorted(...).limit(10)`.
- `FirstAvailableStrategy.selectSlot()` — sorts and picks first: `stream().sorted(...).findFirst()`.

**Q: What is a functional interface? Where do you use one?**
`ParkingEventListener` is annotated `@FunctionalInterface`. Its single abstract method `onEvent(ParkingEvent)` means it can be implemented with a lambda: `publisher.subscribe(event -> dashboardPanel.refresh())`.

---

## 8. JDBC

**Q: Why do you use PreparedStatement?**
PreparedStatement pre-compiles the SQL and uses parameter placeholders (`?`), preventing SQL injection. Every DAO in the project uses PreparedStatement — no string concatenation of user input appears in any query.

**Q: How do you handle transactions?**
`conn.setAutoCommit(false)` → execute all operations → `conn.commit()`. Any exception calls `conn.rollback()`. This is demonstrated in `AllocationService.allocateAndEnter()` and `ParkingService.processExit()`.

**Q: How do you get generated keys?**
`conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)` followed by `ps.getGeneratedKeys()`. Used in `VehicleDAO.insert()`, `ParkingSlotDAO.insert()`, and `ParkingSessionDAO.insert()`.

---

## 9. File I/O and Serialization

**Q: How is CSV export implemented?**
`ReportService.exportSessionsToCsv()` uses `FileWriter` wrapped in `BufferedWriter` (character streams). The header and each data row are written with `bw.write(...)` and `bw.newLine()`. The stream is closed automatically via try-with-resources.

**Q: How is serialization demonstrated?**
`BackupSnapshot` implements `Serializable`. `BackupService.createBackup()` uses `ObjectOutputStream` wrapping `FileOutputStream` to serialise a snapshot. `restoreBackup()` uses `ObjectInputStream` wrapping `FileInputStream` to deserialise and cast it back.

**Q: What is cloning?**
`ParkingSlot` implements `Cloneable` and overrides `clone()`. `BackupSnapshot` stores defensive copies of slots via `slot.clone()`, ensuring the snapshot is not affected by subsequent mutations to the original list.

---

## 10. Fee Calculation

**Q: Walk me through the fee for a 130-minute four-wheeler session.**
Tariff: ₹40 first hour, ₹20 per additional hour.
- First 60 minutes: ₹40.
- Remaining 70 minutes: ceiling(70/60) = 1 additional started hour → ₹20.
- Total: ₹40 + ₹20 = **₹60**.

**Q: Why BigDecimal instead of double for money?**
`double` uses binary floating point and cannot represent all decimal fractions exactly (e.g. 0.1 + 0.2 ≠ 0.3 in binary). `BigDecimal` provides exact decimal arithmetic, essential for monetary calculations.

---

## 11. Database Design

**Q: How do you prevent duplicate active sessions for the same vehicle?**
`AllocationService` checks `sessionDAO.findActiveByVehicleId(vehicleId)` inside the `synchronized` block before inserting a new session. If an active session exists, `InvalidParkingSessionException` is thrown.

**Q: How do you prevent overlapping reservations?**
`ReservationDAO.hasOverlap()` runs inside the same transaction as the insert: `SELECT COUNT(*) ... WHERE slot_id=? AND status='CONFIRMED' AND start_time < newEnd AND end_time > newStart`. If the count > 0, the transaction is rolled back and `ReservationOverlapException` is thrown.

---

## 12. Quick-Fire Questions

| Question | Answer |
|---|---|
| What is autoboxing? | Automatic conversion between primitives and wrapper types (e.g. `int` ↔ `Integer`). Used throughout when adding primitives to collections. |
| What is try-with-resources? | A try statement that automatically closes `AutoCloseable` resources. Used for every JDBC `PreparedStatement` and `ResultSet`. |
| What is `volatile`? | Ensures a variable's latest value is visible to all threads. Used on Singleton `instance` fields. |
| Why `CopyOnWriteArrayList` for listeners? | Iterating the listener list while another thread adds/removes is safe because CopyOnWriteArrayList creates a fresh array copy on every write. |
| What does `SwingUtilities.invokeLater` do? | Schedules a `Runnable` to execute on the EDT, ensuring thread-safe Swing updates. |
| What is a DAO? | Data Access Object — a class that encapsulates all database access for a single entity type. |
