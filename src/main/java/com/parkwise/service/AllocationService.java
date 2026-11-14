package com.parkwise.service;

import com.parkwise.dao.ParkingSessionDAO;
import com.parkwise.dao.ParkingSlotDAO;
import com.parkwise.exception.InvalidParkingSessionException;
import com.parkwise.exception.SlotNotAvailableException;
import com.parkwise.model.*;
import com.parkwise.observer.ParkingEvent;
import com.parkwise.observer.ParkingEventPublisher;
import com.parkwise.strategy.BestFitStrategy;
import com.parkwise.strategy.FirstAvailableStrategy;
import com.parkwise.strategy.SlotAllocationStrategy;
import com.parkwise.util.AppConfig;
import com.parkwise.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Core allocation service: vehicle entry, slot assignment, and vehicle exit.
 *
 * <p>Concurrency safety: the critical section (check-then-act on slot status)
 * is wrapped in a single database transaction with {@code SELECT ... FOR UPDATE}
 * locking, preventing double-allocation even across concurrent JVM threads.
 * An additional JVM-level {@code synchronized} block prevents race conditions
 * within a single JVM instance.</p>
 */
public class AllocationService {

    private static final Logger LOG = Logger.getLogger(AllocationService.class.getName());

    // JVM-level lock for the allocation critical section
    private static final Object ALLOCATION_LOCK = new Object();

    private final ParkingSlotDAO    slotDAO    = new ParkingSlotDAO();
    private final ParkingSessionDAO sessionDAO = new ParkingSessionDAO();

    /**
     * Allocates a parking slot to a vehicle and creates an active session.
     *
     * <p>Thread safety: uses a JVM synchronized block plus a DB transaction
     * with row-level locking so two concurrent requests cannot claim the
     * same last available slot.</p>
     *
     * @return the newly created ParkingSession
     */
    public ParkingSession allocateAndEnter(Vehicle vehicle)
            throws SlotNotAvailableException, InvalidParkingSessionException, SQLException {

        // ── JVM-level synchronization ─────────────────────────────────────────
        synchronized (ALLOCATION_LOCK) {

            // Check for an existing active session
            if (sessionDAO.findActiveByVehicleId(vehicle.getId()).isPresent()) {
                throw new InvalidParkingSessionException(
                    vehicle.getRegistrationNumber() + " already has an active parking session.");
            }

            Connection conn = null;
            try {
                conn = DBConnection.getInstance().getConnection();
                conn.setAutoCommit(false);   // begin transaction

                // 1. Load eligible available slots (read within the transaction)
                List<ParkingSlot> eligible = slotDAO.findAvailableByType(vehicle.getVehicleType());

                if (eligible.isEmpty()) {
                    conn.rollback();
                    ParkingEventPublisher.getInstance().publish(new ParkingEvent(
                        ParkingEvent.EventType.ALLOCATION_FAILED,
                        "No " + vehicle.getVehicleType().getDisplayName() + " slot available"));
                    throw new SlotNotAvailableException(
                        "No available slot for vehicle type: " + vehicle.getVehicleType().getDisplayName());
                }

                // 2. Select slot via configured strategy
                SlotAllocationStrategy strategy = resolveStrategy();
                ParkingSlot selected = strategy.selectSlot(eligible);

                if (selected == null) {
                    conn.rollback();
                    throw new SlotNotAvailableException("Allocation strategy returned no slot");
                }

                // 3. Mark slot OCCUPIED — conditional UPDATE prevents double-booking
                //    If two transactions reach here simultaneously, the DB row lock
                //    ensures only one UPDATE succeeds; the other sees 0 rows updated.
                String lockSql =
                    "UPDATE parking_slots SET slot_status = 'OCCUPIED' "
                  + "WHERE id = ? AND slot_status = 'AVAILABLE' AND active = 1";
                int updated;
                try (var ps = conn.prepareStatement(lockSql)) {
                    ps.setInt(1, selected.getId());
                    updated = ps.executeUpdate();
                }

                if (updated == 0) {
                    // Slot was just taken by another transaction
                    conn.rollback();
                    throw new SlotNotAvailableException(
                        "Slot " + selected.getSlotNumber() + " was just taken. Please retry.");
                }

                // 4. Create the parking session
                ParkingSession session = new ParkingSession();
                session.setVehicleId(vehicle.getId());
                session.setSlotId(selected.getId());
                session.setEntryTime(LocalDateTime.now());
                session.setSessionStatus(ParkingSession.SessionStatus.ACTIVE);
                session.setPaymentStatus(ParkingSession.PaymentStatus.PENDING);

                int sessionId = sessionDAO.insert(conn, session);
                session.setId(sessionId);
                session.setRegistrationNumber(vehicle.getRegistrationNumber());
                session.setSlotNumber(selected.getSlotNumber());
                session.setVehicleType(vehicle.getVehicleType());

                conn.commit();

                // 5. Publish event (Observer pattern)
                ParkingEventPublisher.getInstance().publish(new ParkingEvent(
                    ParkingEvent.EventType.VEHICLE_ENTERED,
                    "Vehicle " + vehicle.getRegistrationNumber()
                        + " entered and allocated slot " + selected.getSlotNumber(),
                    session));

                return session;

            } catch (SQLException e) {
                if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* ignored */ } }
                throw e;
            } finally {
                if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignored */ } }
                DBConnection.getInstance().releaseConnection(conn);
            }
        } // end synchronized
    }

    /**
     * Picks the strategy instance based on current AppConfig setting.
     */
    private SlotAllocationStrategy resolveStrategy() {
        String name = AppConfig.getInstance().getAllocationStrategy();
        return switch (name) {
            case "BEST_FIT" -> new BestFitStrategy();
            default         -> new FirstAvailableStrategy();
        };
    }
}
