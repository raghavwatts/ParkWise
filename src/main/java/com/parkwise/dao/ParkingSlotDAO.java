package com.parkwise.dao;

import com.parkwise.model.ParkingSlot;
import com.parkwise.model.SlotStatus;
import com.parkwise.model.VehicleType;
import com.parkwise.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO for parking slot CRUD and status management.
 */
public class ParkingSlotDAO {

    private static final Logger LOG = Logger.getLogger(ParkingSlotDAO.class.getName());

    /** Inserts a new parking slot; returns the generated ID. */
    public int insert(ParkingSlot slot) throws SQLException {
        String sql = "INSERT INTO parking_slots (slot_number, slot_type, slot_status, active) "
                   + "VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, slot.getSlotNumber().toUpperCase());
                ps.setString(2, slot.getSlotType().name());
                ps.setString(3, slot.getSlotStatus().name());
                ps.setBoolean(4, slot.isActive());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        throw new SQLException("Slot insert returned no generated key");
    }

    /** Returns all slots ordered by slot number. */
    public List<ParkingSlot> findAll() throws SQLException {
        return query("SELECT id, slot_number, slot_type, slot_status, active "
                   + "FROM parking_slots ORDER BY slot_number", null);
    }

    /** Returns available active slots matching the given vehicle type. */
    public List<ParkingSlot> findAvailableByType(VehicleType type) throws SQLException {
        String sql = "SELECT id, slot_number, slot_type, slot_status, active "
                   + "FROM parking_slots "
                   + "WHERE slot_type = ? AND slot_status = 'AVAILABLE' AND active = 1 "
                   + "ORDER BY slot_number";
        Connection conn = null;
        List<ParkingSlot> list = new ArrayList<>();
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, type.name());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    /** Finds a slot by its primary key. */
    public Optional<ParkingSlot> findById(int id) throws SQLException {
        String sql = "SELECT id, slot_number, slot_type, slot_status, active "
                   + "FROM parking_slots WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    /** Finds a slot by its slot number (e.g. "A01"). */
    public Optional<ParkingSlot> findBySlotNumber(String slotNumber) throws SQLException {
        String sql = "SELECT id, slot_number, slot_type, slot_status, active "
                   + "FROM parking_slots WHERE UPPER(slot_number) = UPPER(?)";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, slotNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    /** Updates the full slot record. */
    public void update(ParkingSlot slot) throws SQLException {
        String sql = "UPDATE parking_slots SET slot_number = ?, slot_type = ?, slot_status = ?, active = ? "
                   + "WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, slot.getSlotNumber().toUpperCase());
                ps.setString(2, slot.getSlotType().name());
                ps.setString(3, slot.getSlotStatus().name());
                ps.setBoolean(4, slot.isActive());
                ps.setInt(5, slot.getId());
                ps.executeUpdate();
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates only the status of a slot.
     * Used within transactions when allocating or releasing slots.
     * Accepts an external connection so it participates in the caller's transaction.
     */
    public void updateStatus(Connection conn, int slotId, SlotStatus newStatus) throws SQLException {
        String sql = "UPDATE parking_slots SET slot_status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, slotId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Slot " + slotId + " not found during status update");
        }
    }

    /**
     * Counts slots grouped by status — used for dashboard statistics.
     * Returns int[]: [total, available, occupied, reserved]
     */
    public int[] countByStatus() throws SQLException {
        String sql = "SELECT slot_status, COUNT(*) AS cnt FROM parking_slots WHERE active=1 GROUP BY slot_status";
        int total = 0, available = 0, occupied = 0, reserved = 0;
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    total += cnt;
                    switch (SlotStatus.valueOf(rs.getString("slot_status"))) {
                        case AVAILABLE -> available += cnt;
                        case OCCUPIED  -> occupied  += cnt;
                        case RESERVED  -> reserved  += cnt;
                        default        -> {}
                    }
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return new int[]{total, available, occupied, reserved};
    }

    /** Returns true if a slot with the given slot number already exists. */
    public boolean existsBySlotNumber(String slotNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE UPPER(slot_number) = UPPER(?)";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, slotNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<ParkingSlot> query(String sql, String param) throws SQLException {
        List<ParkingSlot> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (param != null) ps.setString(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    ParkingSlot mapRow(ResultSet rs) throws SQLException {
        ParkingSlot slot = new ParkingSlot();
        slot.setId(rs.getInt("id"));
        slot.setSlotNumber(rs.getString("slot_number"));
        slot.setSlotType(VehicleType.valueOf(rs.getString("slot_type")));
        slot.setSlotStatus(SlotStatus.valueOf(rs.getString("slot_status")));
        slot.setActive(rs.getBoolean("active"));
        return slot;
    }
}
