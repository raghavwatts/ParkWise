package com.parkwise.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a vehicle's parking session from entry to exit.
 * Uses java.time for timestamps and BigDecimal for monetary values.
 */
public class ParkingSession {

    public enum SessionStatus { ACTIVE, COMPLETED, CANCELLED }
    public enum PaymentStatus { PENDING, PAID, WAIVED }

    private int           id;
    private int           vehicleId;
    private int           slotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;       // null while session is ACTIVE
    private Integer       durationMinutes;// null while ACTIVE
    private BigDecimal    feeAmount;      // null while ACTIVE
    private PaymentStatus paymentStatus;
    private SessionStatus sessionStatus;

    // Denormalized fields for display (populated by DAO joins)
    private String        registrationNumber;
    private String        slotNumber;
    private VehicleType   vehicleType;

    public ParkingSession() {}

    // ── Getters ──────────────────────────────────────────────────────────────
    public int            getId()                  { return id; }
    public int            getVehicleId()           { return vehicleId; }
    public int            getSlotId()              { return slotId; }
    public LocalDateTime  getEntryTime()           { return entryTime; }
    public LocalDateTime  getExitTime()            { return exitTime; }
    public Integer        getDurationMinutes()     { return durationMinutes; }
    public BigDecimal     getFeeAmount()           { return feeAmount; }
    public PaymentStatus  getPaymentStatus()       { return paymentStatus; }
    public SessionStatus  getSessionStatus()       { return sessionStatus; }
    public String         getRegistrationNumber()  { return registrationNumber; }
    public String         getSlotNumber()          { return slotNumber; }
    public VehicleType    getVehicleType()         { return vehicleType; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                                { this.id = id; }
    public void setVehicleId(int vehicleId)                  { this.vehicleId = vehicleId; }
    public void setSlotId(int slotId)                        { this.slotId = slotId; }
    public void setEntryTime(LocalDateTime entryTime)        { this.entryTime = entryTime; }
    public void setExitTime(LocalDateTime exitTime)          { this.exitTime = exitTime; }
    public void setDurationMinutes(Integer dur)              { this.durationMinutes = dur; }
    public void setFeeAmount(BigDecimal fee)                 { this.feeAmount = fee; }
    public void setPaymentStatus(PaymentStatus ps)           { this.paymentStatus = ps; }
    public void setSessionStatus(SessionStatus ss)           { this.sessionStatus = ss; }
    public void setRegistrationNumber(String reg)            { this.registrationNumber = reg; }
    public void setSlotNumber(String slotNumber)             { this.slotNumber = slotNumber; }
    public void setVehicleType(VehicleType vehicleType)      { this.vehicleType = vehicleType; }

    @Override
    public String toString() {
        return "Session{id=" + id + ", vehicle=" + registrationNumber
                + ", slot=" + slotNumber + ", status=" + sessionStatus + "}";
    }
}
