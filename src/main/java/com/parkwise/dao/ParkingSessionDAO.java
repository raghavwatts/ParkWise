package com.parkwise.dao;

import com.parkwise.model.ParkingSession;
import com.parkwise.model.ParkingSession.PaymentStatus;
import com.parkwise.model.ParkingSession.SessionStatus;
import com.parkwise.model.VehicleType;
import com.parkwise.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * DAO for parking session operations.
 * Entry/exit operations run in transactions that also update slot status.
 */
public class ParkingSessionDAO {

    private static final Logger LOG = Logger.getLogger(ParkingSessionDAO.class.getName());

    /**
     * Inserts a new parking session using the provided connection.
     * The caller manages the transaction (commit/rollback).
     */
    public int insert(Connection conn, ParkingSession session) throws SQLException {
        String sql = "INSERT INTO parking_sessions "
                   + "(vehicle_id, slot_id, entry_time, session_status, payment_status) "
                   + "VALUES (?, ?, ?, 'ACTIVE', 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, session.getVehicleId());
            ps.setInt(2, session.getSlotId());
            ps.setTimestamp(3, Timestamp.valueOf(session.getEntryTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Session insert returned no generated key");
    }

    /**
     * Completes a session (exit): sets exit time, duration, fee, and status.
     * Uses the provided connection so it participates in the caller's transaction.
     */
    public void completeSession(Connection conn, int sessionId,
                                Timestamp exitTime, int durationMinutes,
                                java.math.BigDecimal feeAmount,
                                PaymentStatus paymentStatus) throws SQLException {
        String sql = "UPDATE parking_sessions "
                   + "SET exit_time = ?, duration_minutes = ?, fee_amount = ?, "
                   + "    payment_status = ?, session_status = 'COMPLETED' "
                   + "WHERE id = ? AND session_status = 'ACTIVE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, exitTime);
            ps.setInt(2, durationMinutes);
            ps.setBigDecimal(3, feeAmount);
            ps.setString(4, paymentStatus.name());
            ps.setInt(5, sessionId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Session " + sessionId + " not found or already completed");
        }
    }

    /** Returns the active session for a vehicle, if any. */
    public Optional<ParkingSession> findActiveByVehicleId(int vehicleId) throws SQLException {
        String sql = baseSelectJoin()
                   + "WHERE ps.vehicle_id = ? AND ps.session_status = 'ACTIVE'";
        return querySingle(sql, vehicleId);
    }

    /** Returns a session by its primary key. */
    public Optional<ParkingSession> findById(int sessionId) throws SQLException {
        String sql = baseSelectJoin() + "WHERE ps.id = ?";
        return querySingle(sql, sessionId);
    }

    /** Returns all active sessions (vehicles currently parked). */
    public List<ParkingSession> findAllActive() throws SQLException {
        String sql = baseSelectJoin()
                   + "WHERE ps.session_status = 'ACTIVE' ORDER BY ps.entry_time";
        return queryList(sql);
    }

    /** Returns all completed sessions for a specific vehicle. */
    public List<ParkingSession> findByVehicleId(int vehicleId) throws SQLException {
        String sql = baseSelectJoin()
                   + "WHERE ps.vehicle_id = ? ORDER BY ps.entry_time DESC";
        return queryListWithParam(sql, vehicleId);
    }

    /** Counts sessions completed today. */
    public int countCompletedToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_sessions "
                   + "WHERE session_status = 'COMPLETED' AND DATE(exit_time) = CURDATE()";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return 0;
    }

    /** Returns today's total revenue from PAID sessions. */
    public java.math.BigDecimal getTodayRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(fee_amount), 0) FROM parking_sessions "
                   + "WHERE payment_status = 'PAID' AND DATE(exit_time) = CURDATE()";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return java.math.BigDecimal.ZERO;
    }

    /** Returns all sessions within a date range (for reports). */
    public List<ParkingSession> findByDateRange(LocalDate from, LocalDate to) throws SQLException {
        String sql = baseSelectJoin()
                   + "WHERE DATE(ps.entry_time) BETWEEN ? AND ? ORDER BY ps.entry_time DESC";
        Connection conn = null;
        List<ParkingSession> list = new ArrayList<>();
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(from));
                ps.setDate(2, Date.valueOf(to));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    /** Returns all sessions for reporting (no date filter). */
    public List<ParkingSession> findAll() throws SQLException {
        return queryList(baseSelectJoin() + "ORDER BY ps.entry_time DESC");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String baseSelectJoin() {
        return "SELECT ps.id, ps.vehicle_id, ps.slot_id, ps.entry_time, ps.exit_time, "
             + "       ps.duration_minutes, ps.fee_amount, ps.payment_status, ps.session_status, "
             + "       v.registration_number, v.vehicle_type, sl.slot_number "
             + "FROM parking_sessions ps "
             + "JOIN vehicles v      ON ps.vehicle_id = v.id "
             + "JOIN parking_slots sl ON ps.slot_id   = sl.id ";
    }

    private Optional<ParkingSession> querySingle(String sql, int param) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    private List<ParkingSession> queryList(String sql) throws SQLException {
        List<ParkingSession> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    private List<ParkingSession> queryListWithParam(String sql, int param) throws SQLException {
        List<ParkingSession> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    ParkingSession mapRow(ResultSet rs) throws SQLException {
        ParkingSession s = new ParkingSession();
        s.setId(rs.getInt("id"));
        s.setVehicleId(rs.getInt("vehicle_id"));
        s.setSlotId(rs.getInt("slot_id"));
        Timestamp entry = rs.getTimestamp("entry_time");
        if (entry != null) s.setEntryTime(entry.toLocalDateTime());
        Timestamp exit = rs.getTimestamp("exit_time");
        if (exit != null) s.setExitTime(exit.toLocalDateTime());
        int dur = rs.getInt("duration_minutes");
        if (!rs.wasNull()) s.setDurationMinutes(dur);
        java.math.BigDecimal fee = rs.getBigDecimal("fee_amount");
        if (fee != null) s.setFeeAmount(fee);
        s.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        s.setSessionStatus(SessionStatus.valueOf(rs.getString("session_status")));
        s.setRegistrationNumber(rs.getString("registration_number"));
        s.setVehicleType(VehicleType.valueOf(rs.getString("vehicle_type")));
        s.setSlotNumber(rs.getString("slot_number"));
        return s;
    }
}
