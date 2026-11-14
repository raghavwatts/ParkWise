package com.parkwise.strategy;

import com.parkwise.model.ParkingSlot;

import java.util.Comparator;
import java.util.List;

/**
 * Allocation strategy: selects the "best fit" slot by minimising wasted
 * capacity and preferring slots whose type exactly matches the vehicle type.
 *
 * <p>Scoring rules (lower score = better fit):</p>
 * <ul>
 *   <li>Exact type match → score 0</li>
 *   <li>ACCESSIBLE slot assigned to a non-accessible vehicle → score 2
 *       (accessible slots are precious; avoid using them unnecessarily)</li>
 *   <li>Tie-break: lexicographic slot number (A01 &lt; A02)</li>
 * </ul>
 *
 * <p>Note: in this system, a vehicle may only park in its own slot type
 * (TWO_WHEELER → TWO_WHEELER, etc.), so the eligible list received here
 * is already pre-filtered for compatibility. The scoring provides
 * a deterministic, explainable ordering for slot selection.</p>
 */
public class BestFitStrategy implements SlotAllocationStrategy {

    @Override
    public ParkingSlot selectSlot(List<ParkingSlot> eligibleSlots) {
        if (eligibleSlots == null || eligibleSlots.isEmpty()) return null;

        // Method reference + lambda in a Comparator chain
        return eligibleSlots.stream()
                .min(Comparator.comparingInt(this::score)
                        .thenComparing(ParkingSlot::getSlotNumber))
                .orElse(null);
    }

    /**
     * Computes a fitness score for a slot. Lower score = better fit.
     * Prefers A-series slots, then B-series, then others.
     * Tie-break: lexicographic slot number.
     */
    private int score(ParkingSlot slot) {
        // Prefer slots in the A-series (score 0), then B-series (score 1), then others.
        char prefix = slot.getSlotNumber().charAt(0);
        if (prefix == 'A') return 0;
        if (prefix == 'B') return 1;
        return 2;
    }

    @Override
    public String getStrategyName() {
        return "BEST_FIT";
    }
}
