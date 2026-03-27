package com.portfoliotracker.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {
    /**
     * Hashes a password using SHA-256 algorithm
     *
     * @param password the plain text password
     * @return the hashed password as hex string
     */
    public static String hashPassword(String password) {
        try {
            // Create SHA-256 digest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Convert password to bytes and hash it
            byte[] hashBytes = digest.digest(password.getBytes());
            // Convert bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    /**
     * @param password   the plain text password to verify
     * @param storedHash the stored hashed password
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        return hashPassword(password).equals(storedHash);
    }
}