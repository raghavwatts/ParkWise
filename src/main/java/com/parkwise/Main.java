package com.parkwise;

import com.parkwise.service.AuthService;
import com.parkwise.ui.LoginScreen;
import com.parkwise.util.DatabaseInitializer;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application entry point for ParkWise.
 *
 * <p>Startup sequence:</p>
 * <ol>
 *   <li>Set the Swing look-and-feel to the system default.</li>
 *   <li>Run database initialization (creates default admin and tariffs if absent).</li>
 *   <li>Launch the login screen on the Event Dispatch Thread.</li>
 * </ol>
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        // 1. Use the system look-and-feel for a native desktop appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not set system look-and-feel", e);
        }

        // 2. Initialise the database (idempotent — safe to run every time)
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Database initialisation failed. Check db.properties.", e);
            JOptionPane.showMessageDialog(null,
                "Database connection failed.\n\n"
              + "Please verify:\n"
              + "  1. MySQL is running.\n"
              + "  2. parkwise_db database exists (run sql/schema.sql).\n"
              + "  3. Credentials in src/main/resources/db.properties are correct.\n\n"
              + "Error: " + e.getMessage(),
                "ParkWise — Startup Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // 3. Launch UI on the EDT (Swing threading rule)
        SwingUtilities.invokeLater(() -> {
            LoginScreen login = new LoginScreen(new AuthService());
            login.setVisible(true);
        });
    }
}
