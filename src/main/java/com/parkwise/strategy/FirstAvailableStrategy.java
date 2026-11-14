package com.parkwise.strategy;

import com.parkwise.model.ParkingSlot;

import java.util.Comparator;
import java.util.List;

/**
 * Allocation strategy: assigns the slot with the lexicographically smallest
 * slot number that is eligible — i.e., the "first" slot in a consistent order.
 *
 * <p>Sorting by slot number ensures deterministic, reproducible allocation
 * (A01 before A02, A-series before B-series, etc.).</p>
 */
public class FirstAvailableStrategy implements SlotAllocationStrategy {

    @Override
    public ParkingSlot selectSlot(List<ParkingSlot> eligibleSlots) {
        if (eligibleSlots == null || eligibleSlots.isEmpty()) return null;

        // Lambda expression + Stream API: sort by slot number, pick first
        return eligibleSlots.stream()
                .sorted(Comparator.comparing(ParkingSlot::getSlotNumber))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getStrategyName() {
        return "FIRST_AVAILABLE";
    }
}
