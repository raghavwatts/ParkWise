package com.parkwise.model;

/**
 * Enum representing supported vehicle categories.
 * Each constant maps directly to the MySQL ENUM column value.
 */
public enum VehicleType {
    TWO_WHEELER("Two-Wheeler"),
    FOUR_WHEELER("Four-Wheeler"),
    ACCESSIBLE("Accessible");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    /** Returns the enum whose displayName matches the given label (case-insensitive). */
    public static VehicleType fromDisplayName(String name) {
        for (VehicleType t : values()) {
            if (t.displayName.equalsIgnoreCase(name)) return t;
        }
        throw new IllegalArgumentException("Unknown vehicle type: " + name);
    }

    @Override
    public String toString() { return displayName; }
}
