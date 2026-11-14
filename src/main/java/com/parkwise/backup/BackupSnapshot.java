package com.parkwise.backup;

import com.parkwise.model.ParkingSlot;
import com.parkwise.model.Vehicle;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable snapshot of vehicle and slot data for backup/restore.
 * Contains only non-sensitive model objects — no database connections,
 * Swing components, or password hashes are serialized.
 * ParkingSlot implements {@link Cloneable}; defensive copies are stored here.
 */
public class BackupSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDateTime  createdAt;
    private final List<Vehicle>  vehicles;
    private final List<ParkingSlot> slots;

    public BackupSnapshot(List<Vehicle> vehicles, List<ParkingSlot> slots) {
        this.createdAt = LocalDateTime.now();
        this.vehicles = new ArrayList<>(vehicles);
        this.slots    = new ArrayList<>();
        slots.forEach(s -> this.slots.add(s.clone()));
    }

    public LocalDateTime     getCreatedAt() { return createdAt; }
    public List<Vehicle>     getVehicles()  { return vehicles; }
    public List<ParkingSlot> getSlots()     { return slots; }

    @Override
    public String toString() {
        return "BackupSnapshot{createdAt=" + createdAt
                + ", vehicles=" + vehicles.size()
                + ", slots=" + slots.size() + "}";
    }
}
