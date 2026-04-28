package com.medicheck.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 * BCrypt automatically generates salt and is resistant to brute-force attacks.
 */
public final class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 12;

    private PasswordUtil() {}

    /**
     * Hash a plain-text password using BCrypt.
     * @param plainPassword the plain-text password
     * @return BCrypt hashed password string
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verify a plain-text password against a stored BCrypt hash.
     * @param plainPassword the entered password
     * @param hashedPassword the stored hash
     * @return true if they match
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate password strength.
     * Must be at least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char.
     */
    public static boolean isStrong(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*()-_+=<>?".indexOf(c) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
