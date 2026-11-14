package com.parkwise.util;

import java.util.regex.Pattern;

/**
 * Stateless validation helpers used throughout the application.
 * All methods are static — this is a pure utility class.
 */
public final class ValidationUtil {

    private static final Pattern REG_NUMBER_PATTERN =
            Pattern.compile("^[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}$");

    private static final Pattern CONTACT_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");   // Indian mobile number

    private static final Pattern SLOT_NUMBER_PATTERN =
            Pattern.compile("^[A-Z]\\d{2}$");   // e.g. A01, B10

    private ValidationUtil() {}

    /** Returns true if the string is null or blank. */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Validates an Indian vehicle registration number (e.g. KA01AB1234). */
    public static boolean isValidRegistrationNumber(String reg) {
        return reg != null && REG_NUMBER_PATTERN.matcher(reg.trim().toUpperCase()).matches();
    }

    /** Validates a 10-digit Indian mobile number. */
    public static boolean isValidContactNumber(String contact) {
        return contact != null && CONTACT_PATTERN.matcher(contact.trim()).matches();
    }

    /** Validates slot number format. */
    public static boolean isValidSlotNumber(String slot) {
        return slot != null && SLOT_NUMBER_PATTERN.matcher(slot.trim().toUpperCase()).matches();
    }

    /**
     * Validates that a password meets minimum requirements:
     * at least 6 characters, at least one digit, one letter.
     */
    public static boolean isValidPassword(String password) {
        if (isBlank(password) || password.length() < 6) return false;
        boolean hasDigit  = password.chars().anyMatch(Character::isDigit);
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        return hasDigit && hasLetter;
    }

    /** Throws IllegalArgumentException with the given message if condition is false. */
    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
