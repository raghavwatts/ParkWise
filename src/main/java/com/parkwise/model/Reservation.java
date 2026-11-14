package com.parkwise.model;

import java.time.LocalDateTime;

/**
 * Represents an advance parking reservation.
 * Overlap prevention is enforced at the DAO/service level using transactions.
 */
public class Reservation {

    public enum ReservationStatus { CONFIRMED, CANCELLED, COMPLETED }

    private int               id;
    private int               vehicleId;
    private int               slotId;
    private LocalDateTime     startTime;
    private LocalDateTime     endTime;
    private ReservationStatus reservationStatus;
    private LocalDateTime     createdAt;

    // Denormalized for display
    private String            registrationNumber;
    private String            slotNumber;
    private String            ownerName;

    public Reservation() {}

    // ── Getters ──────────────────────────────────────────────────────────────
    public int               getId()                  { return id; }
    public int               getVehicleId()           { return vehicleId; }
    public int               getSlotId()              { return slotId; }
    public LocalDateTime     getStartTime()           { return startTime; }
    public LocalDateTime     getEndTime()             { return endTime; }
    public ReservationStatus getReservationStatus()   { return reservationStatus; }
    public LocalDateTime     getCreatedAt()           { return createdAt; }
    public String            getRegistrationNumber()  { return registrationNumber; }
    public String            getSlotNumber()          { return slotNumber; }
    public String            getOwnerName()           { return ownerName; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                                        { this.id = id; }
    public void setVehicleId(int vehicleId)                          { this.vehicleId = vehicleId; }
    public void setSlotId(int slotId)                                { this.slotId = slotId; }
    public void setStartTime(LocalDateTime startTime)                { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime)                    { this.endTime = endTime; }
    public void setReservationStatus(ReservationStatus status)       { this.reservationStatus = status; }
    public void setCreatedAt(LocalDateTime createdAt)                { this.createdAt = createdAt; }
    public void setRegistrationNumber(String reg)                    { this.registrationNumber = reg; }
    public void setSlotNumber(String slotNumber)                     { this.slotNumber = slotNumber; }
    public void setOwnerName(String ownerName)                       { this.ownerName = ownerName; }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", vehicle=" + registrationNumber
                + ", slot=" + slotNumber + ", status=" + reservationStatus + "}";
    }
}
