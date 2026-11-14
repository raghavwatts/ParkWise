package com.parkwise.service;

import com.parkwise.dao.AdminDAO;
import com.parkwise.model.Admin;
import com.parkwise.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service for administrator authentication.
 * Passwords are verified with PasswordUtil; plaintext never stored.
 */
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    private final AdminDAO adminDAO;
    private Admin          currentAdmin;  // logged-in session

    public AuthService() {
        this.adminDAO = new AdminDAO();
    }

    /**
     * Attempts to log in with the provided credentials.
     *
     * @param username plain username
     * @param password plain password
     * @return true on success
     */
    public boolean login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return false;
        }
        Optional<Admin> optAdmin = adminDAO.findByUsername(username.trim());
        if (optAdmin.isEmpty()) return false;

        Admin admin = optAdmin.get();
        if (PasswordUtil.verifyPassword(password, admin.getPasswordHash())) {
            this.currentAdmin = admin;
            LOG.info("Admin logged in: " + username);
            return true;
        }
        return false;
    }

    /** Clears the current session. */
    public void logout() {
        LOG.info("Admin logged out: " + (currentAdmin != null ? currentAdmin.getUsername() : "?"));
        this.currentAdmin = null;
    }

    /** Returns the currently authenticated admin, or null if not logged in. */
    public Admin getCurrentAdmin() { return currentAdmin; }

    /** Returns true if an admin session is active. */
    public boolean isLoggedIn() { return currentAdmin != null; }

    /**
     * Changes the password of the current admin.
     * Requires the old password for verification.
     */
    public boolean changePassword(String oldPassword, String newPassword) throws SQLException {
        if (!isLoggedIn()) return false;
        if (!PasswordUtil.verifyPassword(oldPassword, currentAdmin.getPasswordHash())) return false;
        String newHash = PasswordUtil.hashPassword(newPassword);
        adminDAO.updatePassword(currentAdmin.getId(), newHash);
        currentAdmin.setPasswordHash(newHash);
        return true;
    }
}
