package com.parkwise.strategy;

import com.parkwise.model.ParkingSlot;
import com.parkwise.model.SlotStatus;
import com.parkwise.model.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for slot allocation strategies.
 */
class SlotAllocationStrategyTest {

    // ── Test helpers ──────────────────────────────────────────────────────────

    private ParkingSlot makeSlot(int id, String number, VehicleType type) {
        ParkingSlot s = new ParkingSlot();
        s.setId(id);
        s.setSlotNumber(number);
        s.setSlotType(type);
        s.setSlotStatus(SlotStatus.AVAILABLE);
        s.setActive(true);
        return s;
    }

    // ── FirstAvailableStrategy ────────────────────────────────────────────────

    @Test
    @DisplayName("FirstAvailable: returns slot with lowest slot number")
    void firstAvailable_returnsLowest() {
        List<ParkingSlot> slots = Arrays.asList(
            makeSlot(3, "A03", VehicleType.TWO_WHEELER),
            makeSlot(1, "A01", VehicleType.TWO_WHEELER),
            makeSlot(2, "A02", VehicleType.TWO_WHEELER)
        );
        ParkingSlot result = new FirstAvailableStrategy().selectSlot(slots);
        assertNotNull(result);
        assertEquals("A01", result.getSlotNumber());
    }

    @Test
    @DisplayName("FirstAvailable: returns null for empty list")
    void firstAvailable_emptyList_returnsNull() {
        assertNull(new FirstAvailableStrategy().selectSlot(Collections.emptyList()));
    }

    @Test
    @DisplayName("FirstAvailable: single slot returns that slot")
    void firstAvailable_singleSlot() {
        ParkingSlot slot = makeSlot(1, "B01", VehicleType.FOUR_WHEELER);
        assertEquals(slot, new FirstAvailableStrategy().selectSlot(List.of(slot)));
    }

    // ── BestFitStrategy ───────────────────────────────────────────────────────

    @Test
    @DisplayName("BestFit: returns a slot (non-null) from non-empty list")
    void bestFit_nonEmpty_returnsSlot() {
        List<ParkingSlot> slots = Arrays.asList(
            makeSlot(2, "B02", VehicleType.FOUR_WHEELER),
            makeSlot(1, "B01", VehicleType.FOUR_WHEELER)
        );
        assertNotNull(new BestFitStrategy().selectSlot(slots));
    }

    @Test
    @DisplayName("BestFit: returns null for empty list")
    void bestFit_emptyList_returnsNull() {
        assertNull(new BestFitStrategy().selectSlot(Collections.emptyList()));
    }
}
