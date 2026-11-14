package com.parkwise.util;

import com.parkwise.dao.AdminDAO;
import com.parkwise.dao.TariffDAO;
import com.parkwise.model.Tariff;
import com.parkwise.model.VehicleType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * First-run database initializer.
 * Creates the default admin account and seed tariffs if they do not exist.
 * Called once at application startup.
 *
 * <p>Default development credentials (NEVER use in production):</p>
 * <ul>
 *   <li>Username: {@code admin}</li>
 *   <li>Password: {@code Admin@123}</li>
 * </ul>
 *
 * <p>Change the password immediately after the first login.</p>
 */
public final class DatabaseInitializer {

    private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class.getName());

    private static final String DEFAULT_ADMIN_USER = "admin";
    private static final String DEFAULT_ADMIN_PASS = "Admin@123";

    private DatabaseInitializer() {}

    /**
     * Initializes the database with default data if not already present.
     * Safe to call on every startup; uses existence checks before inserting.
     */
    public static void initialize() {
        try {
            ensureAdmin();
            ensureTariffs();
            LOG.info("Database initialization complete.");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Database initialization failed", e);
        }
    }

    private static void ensureAdmin() throws SQLException {
        AdminDAO adminDAO = new AdminDAO();
        if (!adminDAO.hasAnyAdmin()) {
            String hash = PasswordUtil.hashPassword(DEFAULT_ADMIN_PASS);
            adminDAO.insert(DEFAULT_ADMIN_USER, hash);
            LOG.info("Default admin account created. Username: admin | Password: Admin@123");
            LOG.warning("IMPORTANT: Change the admin password after first login!");
        }
    }

    private static void ensureTariffs() throws SQLException {
        TariffDAO tariffDAO = new TariffDAO();
        if (tariffDAO.findAll().isEmpty()) {
            tariffDAO.insert(new Tariff(0, VehicleType.TWO_WHEELER,
                    new BigDecimal("20.00"), new BigDecimal("10.00"), true));
            tariffDAO.insert(new Tariff(0, VehicleType.FOUR_WHEELER,
                    new BigDecimal("40.00"), new BigDecimal("20.00"), true));
            tariffDAO.insert(new Tariff(0, VehicleType.ACCESSIBLE,
                    new BigDecimal("20.00"), new BigDecimal("10.00"), true));
            LOG.info("Default tariffs inserted.");
        }
    }
}
