package com.parkwise.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ValidationUtil helper methods.
 */
class ValidationUtilTest {

    @Test @DisplayName("Valid registration number passes")
    void validRegNumber() {
        assertTrue(ValidationUtil.isValidRegistrationNumber("KA01AB1234"));
        assertTrue(ValidationUtil.isValidRegistrationNumber("MH02CD5678"));
    }

    @Test @DisplayName("Invalid registration number fails")
    void invalidRegNumber() {
        assertFalse(ValidationUtil.isValidRegistrationNumber("INVALID"));
        assertFalse(ValidationUtil.isValidRegistrationNumber(""));
        assertFalse(ValidationUtil.isValidRegistrationNumber(null));
    }

    @Test @DisplayName("Valid 10-digit contact passes")
    void validContact() {
        assertTrue(ValidationUtil.isValidContactNumber("9876543210"));
        assertTrue(ValidationUtil.isValidContactNumber("6000000000"));
    }

    @Test @DisplayName("Invalid contact fails")
    void invalidContact() {
        assertFalse(ValidationUtil.isValidContactNumber("1234567890")); // starts with 1
        assertFalse(ValidationUtil.isValidContactNumber("98765"));       // too short
        assertFalse(ValidationUtil.isValidContactNumber(null));
    }

    @Test @DisplayName("isBlank returns true for null and empty strings")
    void isBlank() {
        assertTrue(ValidationUtil.isBlank(null));
        assertTrue(ValidationUtil.isBlank(""));
        assertTrue(ValidationUtil.isBlank("   "));
        assertFalse(ValidationUtil.isBlank("hello"));
    }
}
