package com.parkwise.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for salted password hashing.
 */
class PasswordUtilTest {

    @Test
    @DisplayName("Hash and verify round-trip succeeds")
    void hashAndVerify_roundTrip() {
        String password = "Admin@123";
        String hash = PasswordUtil.hashPassword(password);
        assertTrue(PasswordUtil.verifyPassword(password, hash));
    }

    @Test
    @DisplayName("Wrong password does not verify")
    void wrongPassword_doesNotVerify() {
        String hash = PasswordUtil.hashPassword("correct");
        assertFalse(PasswordUtil.verifyPassword("wrong", hash));
    }

    @Test
    @DisplayName("Two hashes of the same password are different (salt randomness)")
    void twoHashes_areDifferent() {
        String h1 = PasswordUtil.hashPassword("same");
        String h2 = PasswordUtil.hashPassword("same");
        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("Both hashes still verify against the same password")
    void twoHashes_bothVerify() {
        String pw = "testPass1";
        String h1 = PasswordUtil.hashPassword(pw);
        String h2 = PasswordUtil.hashPassword(pw);
        assertTrue(PasswordUtil.verifyPassword(pw, h1));
        assertTrue(PasswordUtil.verifyPassword(pw, h2));
    }

    @Test
    @DisplayName("Null stored value returns false without exception")
    void nullStoredValue_returnsFalse() {
        assertFalse(PasswordUtil.verifyPassword("any", null));
    }

    @Test
    @DisplayName("Malformed stored value (no colon) returns false")
    void malformedStoredValue_returnsFalse() {
        assertFalse(PasswordUtil.verifyPassword("any", "nocolon"));
    }
}
