package com.parkwise.model;

import java.io.Serializable;

/**
 * Represents a physical parking slot.
 * Implements Serializable for backup snapshots.
 * Implements Cloneable with a defensive copy constructor.
 */
public class ParkingSlot implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private int         id;
    private String      slotNumber;   // e.g. "A01"
    private VehicleType slotType;
    private SlotStatus  slotStatus;
    private boolean     active;

    public ParkingSlot() {}

    public ParkingSlot(int id, String slotNumber, VehicleType slotType,
                       SlotStatus slotStatus, boolean active) {
        this.id         = id;
        this.slotNumber = slotNumber;
        this.slotType   = slotType;
        this.slotStatus = slotStatus;
        this.active     = active;
    }

    /** Defensive copy constructor — returns an independent copy of the given slot. */
    public ParkingSlot(ParkingSlot other) {
        this.id         = other.id;
        this.slotNumber = other.slotNumber;  // String is immutable, direct copy is safe
        this.slotType   = other.slotType;    // Enum is immutable
        this.slotStatus = other.slotStatus;
        this.active     = other.active;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int        getId()          { return id; }
    public String     getSlotNumber()  { return slotNumber; }
    public VehicleType getSlotType()   { return slotType; }
    public SlotStatus getSlotStatus()  { return slotStatus; }
    public boolean    isActive()       { return active; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                      { this.id = id; }
    public void setSlotNumber(String slotNumber)   { this.slotNumber = slotNumber; }
    public void setSlotType(VehicleType slotType)  { this.slotType = slotType; }
    public void setSlotStatus(SlotStatus status)   { this.slotStatus = status; }
    public void setActive(boolean active)          { this.active = active; }

    /** Returns a deep copy of this slot. */
    @Override
    public ParkingSlot clone() {
        try {
            return (ParkingSlot) super.clone();
        } catch (CloneNotSupportedException e) {
            // Cannot happen since we implement Cloneable
            throw new AssertionError(e);
        }
    }

    @Override
    public String toString() {
        return slotNumber + " [" + slotType.getDisplayName() + "] - " + slotStatus.getDisplayName();
    }
}
