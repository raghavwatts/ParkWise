package com.parkwise.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection utility implementing a simple thread-safe connection pool.
 *
 * <p>Design Pattern: <b>Singleton</b> — only one DBConnection instance exists
 * per JVM, providing a single point of database configuration management
 * without creating unsafe global mutable state (the pool is synchronized).</p>
 *
 * <p>Connection settings are loaded from {@code db.properties} on the classpath,
 * never hardcoded.</p>
 */
public final class DBConnection {

    private static final Logger LOG = Logger.getLogger(DBConnection.class.getName());

    // Singleton instance — volatile ensures visibility across threads
    private static volatile DBConnection instance;

    private final String url;
    private final String username;
    private final String password;

    // Simple pool: a stack of reusable connections
    private final java.util.Deque<Connection> pool;
    private final int poolSize;

    /** Private constructor loads configuration from db.properties. */
    private DBConnection() {
        Properties props = loadProperties();
        this.url      = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");
        this.poolSize = Integer.parseInt(props.getProperty("db.pool.size", "5"));
        this.pool     = new java.util.ArrayDeque<>(poolSize);
        initPool();
    }

    /**
     * Returns the singleton instance (double-checked locking for thread safety).
     */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Obtains a connection from the pool, or creates a new one if the pool
     * is temporarily exhausted.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (!pool.isEmpty()) {
            Connection conn = pool.poll();
            try {
                if (conn != null && !conn.isClosed() && conn.isValid(2)) {
                    return conn;
                }
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Stale connection discarded", e);
            }
        }
        // Pool exhausted or stale — create a fresh connection
        return createConnection();
    }

    /**
     * Returns a connection back to the pool, or closes it if the pool is full.
     */
    public synchronized void releaseConnection(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed()) {
                if (pool.size() < poolSize) {
                    conn.setAutoCommit(true);   // reset any transaction state
                    pool.offer(conn);
                } else {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Error releasing connection", e);
        }
    }

    /** Closes all pooled connections — call on application shutdown. */
    public synchronized void shutdown() {
        while (!pool.isEmpty()) {
            try { pool.poll().close(); } catch (SQLException ignored) {}
        }
        LOG.info("DBConnection pool shut down.");
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void initPool() {
        for (int i = 0; i < Math.min(2, poolSize); i++) {
            try {
                pool.offer(createConnection());
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Could not pre-warm connection " + i, e);
            }
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader()
                                        .getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException(
                    "db.properties not found on classpath. " +
                    "Copy src/main/resources/db.properties and set your credentials.");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
        return props;
    }
}
