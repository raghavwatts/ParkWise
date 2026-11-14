package com.parkwise.dao;

import com.parkwise.model.Vehicle;
import com.parkwise.model.VehicleType;
import com.parkwise.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO for vehicle CRUD operations via JDBC.
 */
public class VehicleDAO {

    private static final Logger LOG = Logger.getLogger(VehicleDAO.class.getName());

    /** Inserts a new vehicle and returns its generated ID. */
    public int insert(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (registration_number, vehicle_type, owner_name, contact_number) "
                   + "VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            // JDBC generated-key retrieval
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, vehicle.getRegistrationNumber().toUpperCase());
                ps.setString(2, vehicle.getVehicleType().name());
                ps.setString(3, vehicle.getOwnerName());
                ps.setString(4, vehicle.getContactNumber());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        throw new SQLException("Vehicle insert did not return a generated key");
    }

    /** Returns all registered vehicles, ordered by registration number. */
    public List<Vehicle> findAll() throws SQLException {
        String sql = "SELECT id, registration_number, vehicle_type, owner_name, contact_number, created_at "
                   + "FROM vehicles ORDER BY registration_number";
        return executeQuery(sql);
    }

    /** Finds a vehicle by its exact registration number (case-insensitive). */
    public Optional<Vehicle> findByRegistrationNumber(String regNum) throws SQLException {
        String sql = "SELECT id, registration_number, vehicle_type, owner_name, contact_number, created_at "
                   + "FROM vehicles WHERE UPPER(registration_number) = UPPER(?)";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, regNum);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    /** Finds a vehicle by its primary key. */
    public Optional<Vehicle> findById(int id) throws SQLException {
        String sql = "SELECT id, registration_number, vehicle_type, owner_name, contact_number, created_at "
                   + "FROM vehicles WHERE id = ?";
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

    /** Search vehicles by partial registration number or owner name. */
    public List<Vehicle> search(String keyword) throws SQLException {
        String sql = "SELECT id, registration_number, vehicle_type, owner_name, contact_number, created_at "
                   + "FROM vehicles WHERE registration_number LIKE ? OR owner_name LIKE ? "
                   + "ORDER BY registration_number";
        Connection conn = null;
        List<Vehicle> result = new ArrayList<>();
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String pattern = "%" + keyword + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) result.add(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return result;
    }

    /** Updates mutable vehicle fields. Registration number cannot be changed. */
    public void update(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE vehicles SET vehicle_type = ?, owner_name = ?, contact_number = ? "
                   + "WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, vehicle.getVehicleType().name());
                ps.setString(2, vehicle.getOwnerName());
                ps.setString(3, vehicle.getContactNumber());
                ps.setInt(4, vehicle.getId());
                ps.executeUpdate();
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    /** Deletes a vehicle by ID. Will fail if active sessions exist (FK constraint). */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    /** Returns true if a vehicle with the given registration number exists. */
    public boolean existsByRegistrationNumber(String regNum) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE UPPER(registration_number) = UPPER(?)";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, regNum);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return false;
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private List<Vehicle> executeQuery(String sql) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
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

    Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getInt("id"));
        v.setRegistrationNumber(rs.getString("registration_number"));
        v.setVehicleType(VehicleType.valueOf(rs.getString("vehicle_type")));
        v.setOwnerName(rs.getString("owner_name"));
        v.setContactNumber(rs.getString("contact_number"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) v.setCreatedAt(ts.toLocalDateTime());
        return v;
    }
}
