package com.parkwise.exception;

/**
 * Thrown when a tariff configuration is missing or contains invalid values,
 * such as a negative fee or a missing vehicle-type entry.
 */
public class InvalidTariffException extends Exception {

    public InvalidTariffException(String message) {
        super(message);
    }

    public InvalidTariffException(String message, Throwable cause) {
        super(message, cause);
    }
}
