package com.parkwise.model;

/** Enum for parking slot occupancy states. */
public enum SlotStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED,
    MAINTENANCE;

    public String getDisplayName() {
        return switch (this) {
            case AVAILABLE   -> "Available";
            case OCCUPIED    -> "Occupied";
            case RESERVED    -> "Reserved";
            case MAINTENANCE -> "Maintenance";
        };
    }
}
