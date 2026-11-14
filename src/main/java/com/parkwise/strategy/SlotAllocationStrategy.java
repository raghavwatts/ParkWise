package com.parkwise.strategy;

import com.parkwise.model.ParkingSlot;

import java.util.List;

/**
 * Strategy interface for parking slot allocation.
 *
 * <p>Design Pattern: <b>Strategy</b> — defines a family of slot-selection
 * algorithms, encapsulates each one, and makes them interchangeable.
 * The service selects a strategy at runtime based on {@link com.parkwise.util.AppConfig}.</p>
 */
public interface SlotAllocationStrategy {

    /**
     * Selects the most appropriate slot from the list of eligible candidates.
     *
     * @param eligibleSlots a non-null, possibly empty list of available slots
     *                      that are already filtered for vehicle-type compatibility
     * @return the chosen slot, or null if eligibleSlots is empty
     */
    ParkingSlot selectSlot(List<ParkingSlot> eligibleSlots);

    /** Human-readable name of this strategy (shown in the UI). */
    String getStrategyName();
}
