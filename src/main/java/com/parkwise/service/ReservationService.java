package com.parkwise.service;

import com.parkwise.dao.ParkingSessionDAO;
import com.parkwise.dao.ParkingSlotDAO;
import com.parkwise.dao.ReservationDAO;
import com.parkwise.exception.ReservationOverlapException;
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
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service for advance reservation management.
 * Overlap prevention and slot reservation are performed inside a
 * single database transaction.
 */
public class ReservationService {

    private static final Logger LOG = Logger.getLogger(ReservationService.class.getName());

    private final ReservationDAO    reservationDAO = new ReservationDAO();
    private final ParkingSlotDAO    slotDAO        = new ParkingSlotDAO();
    private final ParkingSessionDAO sessionDAO     = new ParkingSessionDAO();

    /**
     * Creates a new reservation.
     *
     * @param vehicleId     the registered vehicle
     * @param slotId        specific slot ID, or 0 for automatic allocation
     * @param start         reservation start time
     * @param end           reservation end time
     */
    public Reservation createReservation(int vehicleId, int slotId,
                                         LocalDateTime start, LocalDateTime end)
            throws SQLException, ReservationOverlapException, SlotNotAvailableException {

        if (end.isBefore(start) || end.isEqual(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (start.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("Start time cannot be in the past");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int resolvedSlotId = slotId;

            if (resolvedSlotId == 0) {
                // Auto-allocate: find any available slot of correct type
                // We need the vehicle type — fetch it
                String typeQuery = "SELECT vehicle_type FROM vehicles WHERE id = ?";
                VehicleType vType;
                try (var ps = conn.prepareStatement(typeQuery)) {
                    ps.setInt(1, vehicleId);
                    try (var rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Vehicle not found: " + vehicleId);
                        vType = VehicleType.valueOf(rs.getString("vehicle_type"));
                    }
                }
                List<ParkingSlot> eligible = slotDAO.findAvailableByType(vType);
                SlotAllocationStrategy strategy = resolveStrategy();
                ParkingSlot chosen = strategy.selectSlot(eligible);
                if (chosen == null) {
                    conn.rollback();
                    throw new SlotNotAvailableException("No available slot for vehicle type: " + vType);
                }
                resolvedSlotId = chosen.getId();
            }

            // Check overlap within transaction
            if (reservationDAO.hasOverlap(conn, resolvedSlotId, start, end, 0)) {
                conn.rollback();
                throw new ReservationOverlapException(
                    "The selected slot has an overlapping reservation in the requested time window");
            }

            // Mark slot RESERVED
            slotDAO.updateStatus(conn, resolvedSlotId, SlotStatus.RESERVED);

            // Insert reservation
            Reservation res = new Reservation();
            res.setVehicleId(vehicleId);
            res.setSlotId(resolvedSlotId);
            res.setStartTime(start);
            res.setEndTime(end);
            res.setReservationStatus(Reservation.ReservationStatus.CONFIRMED);
            int id = reservationDAO.insert(conn, res);
            res.setId(id);

            conn.commit();

            ParkingEventPublisher.getInstance().publish(new ParkingEvent(
                ParkingEvent.EventType.RESERVATION_CREATED,
                "Reservation #" + id + " created for vehicle ID " + vehicleId, res));

            return res;

        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* ignored */ } }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignored */ } }
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Cancels an existing CONFIRMED reservation and releases the slot.
     */
    public void cancelReservation(int reservationId) throws SQLException {
        Optional<Reservation> opt = reservationDAO.findById(reservationId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Reservation not found: " + reservationId);

        Reservation res = opt.get();
        if (res.getReservationStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Reservation is not in CONFIRMED state");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            reservationDAO.cancel(reservationId);
            slotDAO.updateStatus(conn, res.getSlotId(), SlotStatus.AVAILABLE);

            conn.commit();

            ParkingEventPublisher.getInstance().publish(new ParkingEvent(
                ParkingEvent.EventType.RESERVATION_CANCELLED,
                "Reservation #" + reservationId + " cancelled"));

        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* ignored */ } }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignored */ } }
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    public List<Reservation> getAllReservations() throws SQLException {
        return reservationDAO.findAll();
    }

    public List<Reservation> getByStatus(Reservation.ReservationStatus status) throws SQLException {
        return reservationDAO.findByStatus(status);
    }

    private SlotAllocationStrategy resolveStrategy() {
        return "BEST_FIT".equals(AppConfig.getInstance().getAllocationStrategy())
                ? new BestFitStrategy()
                : new FirstAvailableStrategy();
    }
}
