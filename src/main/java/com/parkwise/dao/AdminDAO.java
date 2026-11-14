package com.parkwise.dao;

import com.parkwise.model.Admin;
import com.parkwise.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data-access object for administrator operations.
 * All SQL uses PreparedStatement to prevent injection.
 */
public class AdminDAO {

    private static final Logger LOG = Logger.getLogger(AdminDAO.class.getName());

    /**
     * Finds an admin by username.
     *
     * @param username the login username
     * @return Optional containing the Admin if found, empty otherwise
     */
    public Optional<Admin> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, created_at FROM admins WHERE username = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Admin admin = mapRow(rs);
                        return Optional.of(admin);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error finding admin by username: " + username, e);
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    /**
     * Inserts a new admin record (used during database initialization).
     *
     * @param username     the admin username
     * @param passwordHash the salted hash string from PasswordUtil
     */
    public void insert(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO admins (username, password_hash) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, passwordHash);
                ps.executeUpdate();
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Updates the password hash for an existing admin.
     */
    public void updatePassword(int adminId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE admins SET password_hash = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newPasswordHash);
                ps.setInt(2, adminId);
                ps.executeUpdate();
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    /** Checks whether any admin account exists (used for first-run setup). */
    public boolean hasAnyAdmin() {
        String sql = "SELECT COUNT(*) FROM admins";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error checking admin count", e);
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return false;
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setId(rs.getInt("id"));
        admin.setUsername(rs.getString("username"));
        admin.setPasswordHash(rs.getString("password_hash"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) admin.setCreatedAt(ts.toLocalDateTime());
        return admin;
    }
}
