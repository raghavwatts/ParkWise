package com.parkwise.exception;

/**
 * Thrown when an attempt is made to register a vehicle with a
 * registration number that already exists in the system.
 */
public class DuplicateVehicleException extends Exception {

    public DuplicateVehicleException(String message) {
        super(message);
    }
}
