# ParkWise — Architecture Explanation

## Layered Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer (Swing)                     │
│  LoginScreen, MainFrame, DashboardPanel, VehiclePanel,    │
│  SlotPanel, EntryExitPanel, ReservationPanel,            │
│  TariffPanel, ReportPanel, SettingsPanel                 │
└─────────────────────────────────────────────────────────┘
          │  calls (no SQL in button handlers)
          ▼
┌─────────────────────────────────────────────────────────┐
│                   Service Layer (Business)                │
│  AuthService, VehicleService, ParkingSlotService,        │
│  AllocationService, ParkingService, ReservationService, │
│  TariffService                                           │
│                                                           │
│  ┌─ strategy ── SlotAllocationStrategy, ParkingFeeStrategy│
│  ┌─ observer ── ParkingEventPublisher, ParkingEventListener│
│  ┌─ factory  ── NotificationFactory, NotificationService  │
└─────────────────────────────────────────────────────────┘
          │  delegates to
          ▼
┌─────────────────────────────────────────────────────────┐
│                    DAO Layer (JDBC)                       │
│  AdminDAO, VehicleDAO, ParkingSlotDAO,                   │
│  ParkingSessionDAO, ReservationDAO, TariffDAO,          │
│  PaymentDAO                                              │
└─────────────────────────────────────────────────────────┘
          │  uses connection from
          ▼
┌─────────────────────────────────────────────────────────┐
│              Util Layer (Cross-cutting)                   │
│  DBConnection (Singleton), AppConfig, ValidationUtil,    │
│  PasswordUtil, DatabaseInitializer, DashboardRefreshTask│
└─────────────────────────────────────────────────────────┘
          │  connects to
          ▼
┌─────────────────────────────────────────────────────────┐
│                  MySQL Database (parkwise_db)             │
└─────────────────────────────────────────────────────────┘
```

## Package Responsibilities

| Package | Responsibility |
|---|---|
| `model` | Plain entity classes mirroring database tables |
| `dao` | One DAO per table — all SQL is here, uses PreparedStatement |
| `service` | Orchestrates business rules, transactions, events |
| `strategy` | Pluggable algorithms for slot selection and fee calc |
| `observer` | Decoupled event publishing for UI refresh |
| `factory` | Creates notification implementations by channel |
| `exception` | Domain-specific checked exceptions |
| `util` | DB connection pool, config, validation, hashing, threading |
| `report` | Report generation and CSV export via character streams |
| `backup` | Serialization snapshot create/restore |
| `ui` | Swing screens — zero SQL, zero business logic |

## Data Flow: Vehicle Entry (Example)

1. **User** enters a registration number in `EntryExitPanel` and clicks "Enter & Allocate Slot".
2. `EntryExitPanel` calls `VehicleService.findByRegistrationNumber()` → `VehicleDAO` → MySQL.
3. `EntryExitPanel` calls `AllocationService.allocateAndEnter(vehicle)`.
4. `AllocationService`:
   - Acquires JVM `synchronized` lock
   - Checks for an existing active session (via `ParkingSessionDAO`)
   - Opens a DB transaction (`setAutoCommit(false)`)
   - Fetches eligible slots of matching vehicle type
   - Delegates slot selection to the configured `SlotAllocationStrategy`
   - Issues a conditional `UPDATE ... WHERE slot_status='AVAILABLE'` to claim the slot
   - Inserts a new `parking_sessions` row via `ParkingSessionDAO.insert()`
   - Commits the transaction (or rolls back on failure)
   - Releases the JVM lock
5. `AllocationService` publishes a `VEHICLE_ENTERED` event via `ParkingEventPublisher`.
6. The dashboard listener (registered in `MainFrame`) receives the event on a background thread and calls `SwingUtilities.invokeLater()` to refresh the UI on the EDT.
7. `EntryExitPanel` displays the allocated slot number to the user.

## Data Flow: Vehicle Exit

1. User enters registration in `EntryExitPanel` and clicks "Exit & Checkout".
2. `EntryExitPanel` calls `ParkingService.processExit(sessionId, method)`.
3. `ParkingService`:
   - Loads the active session
   - Calculates duration using `java.time.Duration.between(entry, exit)`
   - Fetches the `Tariff` via `TariffService` for the vehicle type
   - Calculates fee via `StandardFeeStrategy.calculate(durationMinutes, tariff)`
   - Opens a DB transaction
   - Updates `parking_sessions` with exit time, duration, fee, status
   - Inserts a `payments` row
   - Updates `parking_slots` to AVAILABLE
   - Commits (or rolls back on any failure)
4. `EntryExitPanel` displays the receipt dialog.
5. Observer publishes a `VEHICLE_EXITED` event; dashboard refreshes.

## Concurrency Model

- **Single JVM**: `synchronized(ALLOCATION_LOCK)` serialises allocation attempts.
- **Database**: Conditional UPDATE (`WHERE slot_status='AVAILABLE'`) prevents double-booking across separate JVMs.
- **Swing**: All UI updates go through `SwingUtilities.invokeLater` or `SwingWorker.done()`.
- **Background refresh**: `ScheduledExecutorService` with daemon threads; `CopyOnWriteArrayList` for listener iteration.
- **Shutdown**: `refreshTask.shutdown()` and `DBConnection.shutdown()` are called on window close.

## Why This Layering Works

- **No SQL in UI**: Every DAO owns its SQL. The UI talks only to services.
- **Swappable strategies**: Switching from `FirstAvailableStrategy` to `BestFitStrategy` requires no code change to `AllocationService` — only an `AppConfig` update.
- **Testability**: Strategies and utilities are pure functions with no DB dependency, so they can be tested in isolation (as shown in the test suite).
- **Transaction integrity**: Every multi-step DB operation uses `setAutoCommit(false)` → operations → `commit()`, with `rollback()` in the `catch` block and connection release in `finally`.
