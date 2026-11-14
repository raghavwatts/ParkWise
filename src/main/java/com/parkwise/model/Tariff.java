package com.parkwise.model;

import java.math.BigDecimal;

/**
 * Configurable fee tariff per vehicle type.
 * Uses BigDecimal for accurate monetary arithmetic.
 */
public class Tariff {

    private int        id;
    private VehicleType vehicleType;
    private BigDecimal  firstHourFee;
    private BigDecimal  additionalHourFee;
    private boolean     active;

    public Tariff() {}

    public Tariff(int id, VehicleType vehicleType,
                  BigDecimal firstHourFee, BigDecimal additionalHourFee, boolean active) {
        this.id               = id;
        this.vehicleType      = vehicleType;
        this.firstHourFee     = firstHourFee;
        this.additionalHourFee= additionalHourFee;
        this.active           = active;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int         getId()                 { return id; }
    public VehicleType getVehicleType()        { return vehicleType; }
    public BigDecimal  getFirstHourFee()       { return firstHourFee; }
    public BigDecimal  getAdditionalHourFee()  { return additionalHourFee; }
    public boolean     isActive()              { return active; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                              { this.id = id; }
    public void setVehicleType(VehicleType vehicleType)    { this.vehicleType = vehicleType; }
    public void setFirstHourFee(BigDecimal fee)            { this.firstHourFee = fee; }
    public void setAdditionalHourFee(BigDecimal fee)       { this.additionalHourFee = fee; }
    public void setActive(boolean active)                  { this.active = active; }

    @Override
    public String toString() {
        return vehicleType.getDisplayName()
                + ": ₹" + firstHourFee + "/1st hr, ₹" + additionalHourFee + "/add. hr";
    }
}
