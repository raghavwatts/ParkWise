package com.parkwise.observer;

/**
 * Observer interface for parking system events.
 *
 * <p>Design Pattern: <b>Observer</b> — implementing classes register with
 * {@link ParkingEventPublisher} to receive notifications when parking
 * session state changes.</p>
 */
@FunctionalInterface
public interface ParkingEventListener {

    /**
     * Called when a parking event is published.
     *
     * @param event the event that occurred
     */
    void onEvent(ParkingEvent event);
}
