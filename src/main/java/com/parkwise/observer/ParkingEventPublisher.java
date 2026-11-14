package com.parkwise.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Central event bus for parking system notifications.
 *
 * <p>Design Pattern: <b>Observer + Singleton</b> — a single publisher
 * instance manages all registered listeners and dispatches events.
 * {@link CopyOnWriteArrayList} makes listener iteration thread-safe
 * without blocking the service thread.</p>
 */
public final class ParkingEventPublisher {

    private static final Logger LOG = Logger.getLogger(ParkingEventPublisher.class.getName());

    private static volatile ParkingEventPublisher instance;

    // CopyOnWriteArrayList is thread-safe for iteration during publish
    private final List<ParkingEventListener> listeners = new CopyOnWriteArrayList<>();

    private ParkingEventPublisher() {}

    public static ParkingEventPublisher getInstance() {
        if (instance == null) {
            synchronized (ParkingEventPublisher.class) {
                if (instance == null) instance = new ParkingEventPublisher();
            }
        }
        return instance;
    }

    /** Registers a listener to receive all future events. */
    public void subscribe(ParkingEventListener listener) {
        listeners.add(listener);
    }

    /** Removes a previously registered listener. */
    public void unsubscribe(ParkingEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Publishes an event to all registered listeners.
     * Each listener is called on the publishing thread;
     * Swing listeners must use SwingUtilities.invokeLater internally.
     */
    public void publish(ParkingEvent event) {
        LOG.fine("Publishing event: " + event);
        // Lambda / method reference: listeners.forEach(l -> l.onEvent(event))
        listeners.forEach(l -> {
            try {
                l.onEvent(event);
            } catch (Exception e) {
                LOG.warning("Listener threw exception for event " + event + ": " + e.getMessage());
            }
        });
    }
}
