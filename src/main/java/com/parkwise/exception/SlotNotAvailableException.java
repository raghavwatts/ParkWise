package com.parkwise.exception;

/**
 * Thrown when no eligible parking slot can be found for a vehicle,
 * or when a specific requested slot is not in AVAILABLE state.
 */
public class SlotNotAvailableException extends Exception {

    public SlotNotAvailableException(String message) {
        super(message);
    }

    public SlotNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
