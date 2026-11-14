package com.parkwise.dao;

import com.parkwise.model.Payment;
import com.parkwise.model.Payment.PaymentMethod;
import com.parkwise.model.Payment.PaymentStatus;
import com.parkwise.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO for payment records.
 */
public class PaymentDAO {

    private static final Logger LOG = Logger.getLogger(PaymentDAO.class.getName());

    /** Inserts a payment record using an external connection (participates in transaction). */
    public int insert(Connection conn, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (session_id, amount, payment_method, payment_status) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getSessionId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod().name());
            ps.setString(4, payment.getPaymentStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Payment insert returned no generated key");
    }

    /** Returns all payments for monthly revenue reporting. */
    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT id, session_id, amount, payment_method, payment_status, paid_at "
                   + "FROM payments ORDER BY paid_at DESC";
        List<Payment> list = new ArrayList<>();
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

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setSessionId(rs.getInt("session_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        p.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        Timestamp ts = rs.getTimestamp("paid_at");
        if (ts != null) p.setPaidAt(ts.toLocalDateTime());
        return p;
    }
}
