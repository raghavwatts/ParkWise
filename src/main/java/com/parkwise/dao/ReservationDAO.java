package com.parkwise.dao;

import com.parkwise.model.Reservation;
import com.parkwise.model.Reservation.ReservationStatus;
import com.parkwise.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * DAO for reservation management.
 * Overlap detection uses a SQL range query within the caller's transaction.
 */
public class ReservationDAO {

    private static final Logger LOG = Logger.getLogger(ReservationDAO.class.getName());

    /**
     * Inserts a new reservation using the given connection.
     * The caller is responsible for the surrounding transaction.
     */
    public int insert(Connection conn, Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (vehicle_id, slot_id, start_time, end_time, reservation_status) "
                   + "VALUES (?, ?, ?, ?, 'CONFIRMED')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getVehicleId());
            ps.setInt(2, reservation.getSlotId());
            ps.setTimestamp(3, Timestamp.valueOf(reservation.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(reservation.getEndTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Reservation insert returned no generated key");
    }

    /**
     * Returns true if an overlapping CONFIRMED reservation exists for the given
     * slot and time window, using the caller's connection (within a transaction).
     *
     * <p>Overlap condition: existing.start &lt; new.end AND existing.end &gt; new.start</p>
     */
    public boolean hasOverlap(Connection conn, int slotId,
                              LocalDateTime start, LocalDateTime end,
                              int excludeReservationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations "
                   + "WHERE slot_id = ? AND reservation_status = 'CONFIRMED' AND id != ? "
                   + "AND start_time < ? AND end_time > ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.setInt(2, excludeReservationId);
            ps.setTimestamp(3, Timestamp.valueOf(end));
            ps.setTimestamp(4, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Returns the active confirmed reservation for a vehicle (if any). */
    public Optional<Reservation> findActiveByVehicleId(int vehicleId) throws SQLException {
        String sql = baseSelectJoin()
                   + "WHERE r.vehicle_id = ? AND r.reservation_status = 'CONFIRMED' "
                   + "ORDER BY r.start_time LIMIT 1";
        return querySingle(sql, vehicleId);
    }

    /** Returns all reservations. */
    public List<Reservation> findAll() throws SQLException {
        return queryList(baseSelectJoin() + "ORDER BY r.start_time DESC");
    }

    /** Returns reservations filtered by status. */
    public List<Reservation> findByStatus(ReservationStatus status) throws SQLException {
        String sql = baseSelectJoin()
                   + "WHERE r.reservation_status = ? ORDER BY r.start_time";
        List<Reservation> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.name());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    /** Cancels a reservation by ID. */
    public void cancel(int reservationId) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = 'CANCELLED' WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, reservationId);
                ps.executeUpdate();
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    /** Marks a reservation as COMPLETED. */
    public void complete(Connection conn, int reservationId) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = 'COMPLETED' WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    public Optional<Reservation> findById(int id) throws SQLException {
        String sql = baseSelectJoin() + "WHERE r.id = ?";
        return querySingle(sql, id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String baseSelectJoin() {
        return "SELECT r.id, r.vehicle_id, r.slot_id, r.start_time, r.end_time, "
             + "       r.reservation_status, r.created_at, "
             + "       v.registration_number, v.owner_name, sl.slot_number "
             + "FROM reservations r "
             + "JOIN vehicles v       ON r.vehicle_id = v.id "
             + "JOIN parking_slots sl ON r.slot_id    = sl.id ";
    }

    private Optional<Reservation> querySingle(String sql, int param) throws SQLException {
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

    private List<Reservation> queryList(String sql) throws SQLException {
        List<Reservation> list = new ArrayList<>();
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

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setSlotId(rs.getInt("slot_id"));
        r.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        r.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        r.setReservationStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());
        r.setRegistrationNumber(rs.getString("registration_number"));
        r.setOwnerName(rs.getString("owner_name"));
        r.setSlotNumber(rs.getString("slot_number"));
        return r;
    }
}
