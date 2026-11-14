package com.parkwise.util;

import com.parkwise.observer.ParkingEvent;
import com.parkwise.observer.ParkingEventPublisher;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Background task that periodically triggers a dashboard refresh.
 * Uses a {@link ScheduledExecutorService} with a daemon thread so it does
 * not block JVM shutdown. Call {@code shutdown()} on application close.
 */
public final class DashboardRefreshTask {

    private static final Logger LOG = Logger.getLogger(DashboardRefreshTask.class.getName());

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> future;

    public DashboardRefreshTask() {
        // Thread factory: daemon thread, low priority
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "DashboardRefresh");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
    }

    /** Starts the refresh task with the given interval in seconds. */
    public void start(int intervalSeconds) {
        future = scheduler.scheduleAtFixedRate(() -> {
            try {
                ParkingEventPublisher.getInstance().publish(
                    new ParkingEvent(ParkingEvent.EventType.DASHBOARD_REFRESH, "Auto-refresh"));
            } catch (Exception e) {
                LOG.warning("Dashboard refresh error: " + e.getMessage());
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        LOG.info("Dashboard auto-refresh started (every " + intervalSeconds + "s)");
    }

    /** Stops the refresh task. */
    public void stop() {
        if (future != null) future.cancel(false);
    }

    /** Shuts down the executor — call on application close. */
    public void shutdown() {
        stop();
        scheduler.shutdownNow();
        LOG.info("Dashboard refresh executor shut down");
    }
}
