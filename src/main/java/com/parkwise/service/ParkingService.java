package com.parkwise.service;

import com.parkwise.dao.PaymentDAO;
import com.parkwise.dao.ParkingSessionDAO;
import com.parkwise.dao.ParkingSlotDAO;
import com.parkwise.exception.InvalidParkingSessionException;
import com.parkwise.exception.InvalidTariffException;
import com.parkwise.exception.SlotNotAvailableException;
import com.parkwise.exception.VehicleNotFoundException;
import com.parkwise.model.*;
import com.parkwise.observer.ParkingEvent;
import com.parkwise.observer.ParkingEventPublisher;
import com.parkwise.strategy.ParkingFeeStrategy;
import com.parkwise.strategy.StandardFeeStrategy;
import com.parkwise.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Core service orchestrating vehicle exit, fee calculation, and payment.
 * Uses java.time for accurate duration, BigDecimal for monetary values,
 * and JDBC transactions to ensure atomicity.
 */
public class ParkingService {

    private static final Logger LOG = Logger.getLogger(ParkingService.class.getName());

    private final ParkingSessionDAO sessionDAO = new ParkingSessionDAO();
    private final ParkingSlotDAO    slotDAO    = new ParkingSlotDAO();
    private final PaymentDAO        paymentDAO = new PaymentDAO();
    private final TariffService     tariffService;
    private final ParkingFeeStrategy feeStrategy;

    public ParkingService(TariffService tariffService) {
        this.tariffService = tariffService;
        this.feeStrategy   = new StandardFeeStrategy();
    }

    /**
     * Processes vehicle exit: calculates fee, records payment, releases slot.
     * The entire operation is atomic; a failure triggers a full rollback.
     *
     * @param sessionId     the active session ID
     * @param paymentMethod cash / card / UPI
     * @return the completed ParkingSession with fee and duration set
     */
    public ParkingSession processExit(int sessionId, Payment.PaymentMethod paymentMethod)
            throws InvalidParkingSessionException, InvalidTariffException, SQLException {

        // Load the active session
        Optional<ParkingSession> opt = sessionDAO.findById(sessionId);
        if (opt.isEmpty() || opt.get().getSessionStatus() != ParkingSession.SessionStatus.ACTIVE) {
            throw new InvalidParkingSessionException("Session " + sessionId + " not found or not active");
        }
        ParkingSession session = opt.get();

        // Calculate duration using java.time API
        LocalDateTime exitTime  = LocalDateTime.now();
        LocalDateTime entryTime = session.getEntryTime();
        if (exitTime.isBefore(entryTime)) {
            throw new InvalidParkingSessionException("Exit time is before entry time — system clock issue");
        }

        long totalMinutes = Duration.between(entryTime, exitTime).toMinutes();
        if (totalMinutes < 0) totalMinutes = 0;

        // Resolve tariff and calculate fee via strategy
        Tariff tariff = tariffService.getTariffForType(session.getVehicleType())
                .orElseThrow(() -> new InvalidTariffException(
                    "No active tariff for type: " + session.getVehicleType()));

        BigDecimal fee = feeStrategy.calculate((int) totalMinutes, tariff);

        // Atomic transaction: complete session + insert payment + release slot
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Complete the session
            sessionDAO.completeSession(conn, sessionId,
                    Timestamp.valueOf(exitTime),
                    (int) totalMinutes,
                    fee,
                    ParkingSession.PaymentStatus.PAID);

            // 2. Record payment
            Payment payment = new Payment();
            payment.setSessionId(sessionId);
            payment.setAmount(fee);
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
            paymentDAO.insert(conn, payment);

            // 3. Release the parking slot
            slotDAO.updateStatus(conn, session.getSlotId(), SlotStatus.AVAILABLE);

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* ignored */ } }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignored */ } }
            DBConnection.getInstance().releaseConnection(conn);
        }

        // Update the session object for return
        session.setExitTime(exitTime);
        session.setDurationMinutes((int) totalMinutes);
        session.setFeeAmount(fee);
        session.setPaymentStatus(ParkingSession.PaymentStatus.PAID);
        session.setSessionStatus(ParkingSession.SessionStatus.COMPLETED);

        ParkingEventPublisher.getInstance().publish(new ParkingEvent(
            ParkingEvent.EventType.VEHICLE_EXITED,
            "Vehicle " + session.getRegistrationNumber()
                + " exited from slot " + session.getSlotNumber()
                + " | Fee: ₹" + fee,
            session));

        return session;
    }

    /** Returns the active session for a vehicle, if any. */
    public Optional<ParkingSession> getActiveSession(int vehicleId) throws SQLException {
        return sessionDAO.findActiveByVehicleId(vehicleId);
    }

    /** Returns all currently active sessions. */
    public List<ParkingSession> getAllActiveSessions() throws SQLException {
        return sessionDAO.findAllActive();
    }

    /** Dashboard: count of completed sessions today. */
    public int getCompletedTodayCount() throws SQLException {
        return sessionDAO.countCompletedToday();
    }

    /** Dashboard: today's total revenue. */
    public BigDecimal getTodayRevenue() throws SQLException {
        return sessionDAO.getTodayRevenue();
    }
}
