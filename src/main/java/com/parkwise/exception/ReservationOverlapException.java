package com.parkwise.exception;

/**
 * Thrown when a new reservation's time range overlaps an existing
 * confirmed reservation for the same slot.
 */
public class ReservationOverlapException extends Exception {

    public ReservationOverlapException(String message) {
        super(message);
    }
}
