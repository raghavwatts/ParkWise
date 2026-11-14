package com.parkwise.dao;

import com.parkwise.model.Tariff;
import com.parkwise.model.VehicleType;
import com.parkwise.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * DAO for tariff CRUD.
 */
public class TariffDAO {

    private static final Logger LOG = Logger.getLogger(TariffDAO.class.getName());

    public List<Tariff> findAll() throws SQLException {
        String sql = "SELECT id, vehicle_type, first_hour_fee, additional_hour_fee, active "
                   + "FROM tariffs ORDER BY vehicle_type";
        List<Tariff> list = new ArrayList<>();
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

    public Optional<Tariff> findByVehicleType(VehicleType type) throws SQLException {
        String sql = "SELECT id, vehicle_type, first_hour_fee, additional_hour_fee, active "
                   + "FROM tariffs WHERE vehicle_type = ? AND active = 1";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, type.name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    public void update(Tariff tariff) throws SQLException {
        String sql = "UPDATE tariffs SET first_hour_fee = ?, additional_hour_fee = ?, active = ? "
                   + "WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, tariff.getFirstHourFee());
                ps.setBigDecimal(2, tariff.getAdditionalHourFee());
                ps.setBoolean(3, tariff.isActive());
                ps.setInt(4, tariff.getId());
                ps.executeUpdate();
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    public void insert(Tariff tariff) throws SQLException {
        String sql = "INSERT INTO tariffs (vehicle_type, first_hour_fee, additional_hour_fee, active) "
                   + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                   + "first_hour_fee = VALUES(first_hour_fee), additional_hour_fee = VALUES(additional_hour_fee)";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tariff.getVehicleType().name());
                ps.setBigDecimal(2, tariff.getFirstHourFee());
                ps.setBigDecimal(3, tariff.getAdditionalHourFee());
                ps.setBoolean(4, tariff.isActive());
                ps.executeUpdate();
            }
        } finally {
            DBConnection.getInstance().releaseConnection(conn);
        }
    }

    private Tariff mapRow(ResultSet rs) throws SQLException {
        return new Tariff(
            rs.getInt("id"),
            VehicleType.valueOf(rs.getString("vehicle_type")),
            rs.getBigDecimal("first_hour_fee"),
            rs.getBigDecimal("additional_hour_fee"),
            rs.getBoolean("active")
        );
    }
}
