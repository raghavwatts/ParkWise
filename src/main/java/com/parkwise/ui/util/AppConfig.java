package com.parkwise.util;

/**
 * Application-wide configuration constants.
 *
 * <p>Design Pattern: <b>Singleton</b> — AppConfig ensures a single, consistent
 * view of application-level settings. It is intentionally lightweight;
 * DB credentials live in db.properties, not here.</p>
 */
public final class AppConfig {

    private static volatile AppConfig instance;

    // Allocation strategy: "FIRST_AVAILABLE" | "BEST_FIT"
    private String allocationStrategy = "FIRST_AVAILABLE";

    // Application name shown in window title bars
    public static final String APP_NAME = "ParkWise — Intelligent Parking Allocation System";

    // Date-time format used for display and CSV export
    public static final String DATETIME_FORMAT = "dd-MM-yyyy HH:mm";

    // CSV export directory (relative to user home)
    public static final String EXPORT_DIR = System.getProperty("user.home") + "/ParkWise/exports";

    // Backup directory
    public static final String BACKUP_DIR = System.getProperty("user.home") + "/ParkWise/backups";

    private AppConfig() {}

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) instance = new AppConfig();
            }
        }
        return instance;
    }

    public String getAllocationStrategy()              { return allocationStrategy; }
    public void   setAllocationStrategy(String strat) { this.allocationStrategy = strat; }
}
