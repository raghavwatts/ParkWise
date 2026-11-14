package com.parkwise.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a registered vehicle.
 * Encapsulates all vehicle details with proper access control.
 * Implements Serializable so it can be included in a {@link com.parkwise.backup.BackupSnapshot}.
 */
public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    private int           id;
    private String        registrationNumber;  // must be unique
    private VehicleType   vehicleType;
    private String        ownerName;
    private String        contactNumber;
    private LocalDateTime createdAt;

    public Vehicle() {}

    public Vehicle(int id, String registrationNumber, VehicleType vehicleType,
                   String ownerName, String contactNumber, LocalDateTime createdAt) {
        this.id                 = id;
        this.registrationNumber = registrationNumber;
        this.vehicleType        = vehicleType;
        this.ownerName          = ownerName;
        this.contactNumber      = contactNumber;
        this.createdAt          = createdAt;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int           getId()                   { return id; }
    public String        getRegistrationNumber()   { return registrationNumber; }
    public VehicleType   getVehicleType()          { return vehicleType; }
    public String        getOwnerName()            { return ownerName; }
    public String        getContactNumber()        { return contactNumber; }
    public LocalDateTime getCreatedAt()            { return createdAt; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                                  { this.id = id; }
    public void setRegistrationNumber(String regNum)           { this.registrationNumber = regNum; }
    public void setVehicleType(VehicleType vehicleType)        { this.vehicleType = vehicleType; }
    public void setOwnerName(String ownerName)                 { this.ownerName = ownerName; }
    public void setContactNumber(String contactNumber)         { this.contactNumber = contactNumber; }
    public void setCreatedAt(LocalDateTime createdAt)          { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return registrationNumber + " (" + vehicleType.getDisplayName() + ") - " + ownerName;
    }
}
