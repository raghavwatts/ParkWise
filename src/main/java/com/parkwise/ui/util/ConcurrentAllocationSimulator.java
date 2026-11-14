package com.parkwise.util;

import com.parkwise.exception.InvalidParkingSessionException;
import com.parkwise.exception.SlotNotAvailableException;
import com.parkwise.model.ParkingSession;
import com.parkwise.model.Vehicle;
import com.parkwise.service.AllocationService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Simulates concurrent parking allocation requests to verify that the
 * synchronized allocation logic prevents double-booking under load.
 * Used by the Settings screen's "Run Concurrent Test" button and in unit tests.
 */
public class ConcurrentAllocationSimulator {

    private static final Logger LOG = Logger.getLogger(ConcurrentAllocationSimulator.class.getName());

    private final AllocationService allocationService = new AllocationService();

    /**
     * Launches {@code threadCount} simultaneous allocation attempts for the
     * given list of vehicles and returns the results.
     *
     * @param vehicles    vehicles to allocate (one per thread)
     * @return list of result strings ("SUCCESS: slot X" or "FAILED: reason")
     */
    public List<String> simulate(List<Vehicle> vehicles) throws InterruptedException {
        int threadCount = vehicles.size();
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        // CountDownLatch ensures all threads start at the same instant
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (Vehicle v : vehicles) {
            futures.add(pool.submit(() -> {
                startLatch.await(); // wait for go signal
                try {
                    ParkingSession session = allocationService.allocateAndEnter(v);
                    return "SUCCESS: " + v.getRegistrationNumber()
                           + " → slot " + session.getSlotNumber();
                } catch (SlotNotAvailableException e) {
                    return "FAILED (no slot): " + v.getRegistrationNumber() + " — " + e.getMessage();
                } catch (InvalidParkingSessionException e) {
                    return "FAILED (session): " + v.getRegistrationNumber() + " — " + e.getMessage();
                } catch (SQLException e) {
                    return "ERROR: " + v.getRegistrationNumber() + " — " + e.getMessage();
                }
            }));
        }

        startLatch.countDown(); // release all threads simultaneously

        List<String> results = new ArrayList<>();
        for (Future<String> f : futures) {
            try {
                results.add(f.get(10, TimeUnit.SECONDS));
            } catch (ExecutionException | TimeoutException e) {
                results.add("TIMEOUT/ERROR: " + e.getMessage());
            }
        }

        pool.shutdown();
        pool.awaitTermination(15, TimeUnit.SECONDS);
        return results;
    }
}
