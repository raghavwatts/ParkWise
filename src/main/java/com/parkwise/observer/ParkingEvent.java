package com.parkwise.observer;

/**
 * Represents an event in the parking system lifecycle.
 *
 * <p>Design Pattern: <b>Observer</b> — events are published by the service
 * layer and consumed by registered listeners (UI refresh, notification, etc.).</p>
 */
public class ParkingEvent {

    public enum EventType {
        VEHICLE_ENTERED,
        VEHICLE_EXITED,
        SLOT_ALLOCATED,
        ALLOCATION_FAILED,
        RESERVATION_CREATED,
        RESERVATION_CANCELLED,
        DASHBOARD_REFRESH
    }

    private final EventType eventType;
    private final String    message;
    private final Object    payload;    // optional data object (session, slot, etc.)

    public ParkingEvent(EventType eventType, String message) {
        this(eventType, message, null);
    }

    public ParkingEvent(EventType eventType, String message, Object payload) {
        this.eventType = eventType;
        this.message   = message;
        this.payload   = payload;
    }

    public EventType getEventType() { return eventType; }
    public String    getMessage()   { return message; }
    public Object    getPayload()   { return payload; }

    @Override
    public String toString() {
        return "[" + eventType + "] " + message;
    }
}
