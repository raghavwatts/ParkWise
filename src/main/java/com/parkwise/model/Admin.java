package com.parkwise.model;

import java.time.LocalDateTime;

/**
 * Represents an administrator account.
 * Passwords are never stored in plaintext; only the salted hash is held.
 */
public class Admin {

    private int id;
    private String username;
    /** Format: <hex-salt>:<hex-sha256-hash>  — never expose raw password. */
    private String passwordHash;
    private LocalDateTime createdAt;

    public Admin() {}

    public Admin(int id, String username, String passwordHash, LocalDateTime createdAt) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.createdAt    = createdAt;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int           getId()           { return id; }
    public String        getUsername()     { return username; }
    public String        getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedAt()    { return createdAt; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                     { this.id = id; }
    public void setUsername(String username)      { this.username = username; }
    public void setPasswordHash(String hash)      { this.passwordHash = hash; }
    public void setCreatedAt(LocalDateTime dt)    { this.createdAt = dt; }

    @Override
    public String toString() {
        return "Admin{id=" + id + ", username='" + username + "'}";
    }
}
