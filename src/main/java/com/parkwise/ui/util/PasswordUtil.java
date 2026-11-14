package com.parkwise.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for salted SHA-256 password hashing.
 *
 * <p>Storage format: {@code <16-byte-hex-salt>:<64-byte-hex-sha256>}</p>
 *
 * <p>A proper production system would use bcrypt or Argon2; SHA-256 is
 * chosen here because it is available in the standard JDK without extra
 * libraries, satisfying the project's dependency constraint while still
 * being far safer than plaintext storage.</p>
 */
public final class PasswordUtil {

    private static final int SALT_BYTES = 16;

    private PasswordUtil() {}   // utility class — no instantiation

    /**
     * Hashes a plaintext password with a freshly generated random salt.
     *
     * @param plaintext the raw password
     * @return stored string in format {@code salt:hash}
     */
    public static String hashPassword(String plaintext) {
        byte[] saltBytes = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(saltBytes);
        String salt = bytesToHex(saltBytes);
        String hash = sha256(salt + plaintext);
        return salt + ":" + hash;
    }

    /**
     * Verifies a plaintext password against a stored {@code salt:hash} string.
     *
     * @param plaintext   the password to verify
     * @param storedValue the value from the database column
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plaintext, String storedValue) {
        if (storedValue == null || !storedValue.contains(":")) return false;
        String[] parts = storedValue.split(":", 2);
        String salt     = parts[0];
        String expected = parts[1];
        String actual   = sha256(salt + plaintext);
        return constantTimeEquals(expected, actual);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return bytesToHex(md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JDK spec — this cannot happen
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** Converts a byte array to a lowercase hex string. */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * Constant-time string comparison to prevent timing-based side-channel attacks.
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            // XOR of Character values for constant-time comparison
            result |= (Character.compare(a.charAt(i), b.charAt(i)));
        }
        return result == 0;
    }
}
