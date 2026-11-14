package com.parkwise.exception;

/**
 * Thrown when a vehicle lookup by registration number or ID returns no result.
 */
public class VehicleNotFoundException extends Exception {

    public VehicleNotFoundException(String message) {
        super(message);
    }

    public VehicleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
