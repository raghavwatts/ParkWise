package com.parkwise.exception;

/**
 * Thrown when a parking session operation is invalid, for example:
 * attempting to check out a vehicle that is not currently parked, or
 * double-entry of the same vehicle.
 */
public class InvalidParkingSessionException extends Exception {

    public InvalidParkingSessionException(String message) {
        super(message);
    }

    public InvalidParkingSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
