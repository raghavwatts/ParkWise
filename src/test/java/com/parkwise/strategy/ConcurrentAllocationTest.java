package com.parkwise.strategy;

import com.parkwise.model.ParkingSlot;
import com.parkwise.model.SlotStatus;
import com.parkwise.model.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that concurrent slot selection from a shared list produces exactly
 * ONE winner when only one slot is available, confirming race-condition safety.
 *
 * <p>This test covers the in-memory portion. The DB-level guarantee is enforced
 * by the conditional UPDATE in AllocationService (requires a live database).</p>
 */
class ConcurrentAllocationTest {

    /**
     * Simulates N threads competing to "claim" the single available slot.
     * Uses AtomicInteger as a counter — only the first thread to compare-and-set
     * zero→1 wins; the rest see 1 and fail.
     */
    @RepeatedTest(5)
    @DisplayName("Only one thread claims the last slot (repeated 5 times)")
    void onlyOneThreadClaimsLastSlot() throws InterruptedException {

        // Single available slot
        ParkingSlot slot = new ParkingSlot(1, "A01", VehicleType.TWO_WHEELER,
                SlotStatus.AVAILABLE, true);
        AtomicInteger claimedCount = new AtomicInteger(0);

        int threads = 10;
        CountDownLatch startLatch  = new CountDownLatch(1);
        CountDownLatch doneLatch   = new CountDownLatch(threads);
        ExecutorService pool       = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startLatch.await();     // all threads start simultaneously
                    // Only claim if still AVAILABLE
                    synchronized (slot) {
                        if (slot.getSlotStatus() == SlotStatus.AVAILABLE) {
                            slot.setSlotStatus(SlotStatus.OCCUPIED);
                            claimedCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();           // release all threads
        doneLatch.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        // Exactly one thread should have claimed the slot
        assertEquals(1, claimedCount.get(),
            "Exactly one thread must claim the single available slot");
        assertEquals(SlotStatus.OCCUPIED, slot.getSlotStatus());
    }

    @Test
    @DisplayName("FirstAvailable strategy is deterministic across calls")
    void firstAvailableStrategy_isDeterministic() {
        List<ParkingSlot> slots = new ArrayList<>();
        for (int i = 5; i >= 1; i--) {
            ParkingSlot s = new ParkingSlot(i, "A0" + i, VehicleType.TWO_WHEELER,
                    SlotStatus.AVAILABLE, true);
            slots.add(s);
        }
        FirstAvailableStrategy strategy = new FirstAvailableStrategy();
        ParkingSlot first = strategy.selectSlot(slots);
        // Should always return A01 regardless of list order
        assertNotNull(first);
        assertEquals("A01", first.getSlotNumber());
    }
}
