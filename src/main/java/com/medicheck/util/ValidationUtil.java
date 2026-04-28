package com.medicheck.util;

import java.util.regex.Pattern;

/**
 * Centralized input validation utility for forms and data integrity.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z .'-]{2,100}$");
    private static final Pattern ALPHANUMERIC_PATTERN =
            Pattern.compile("^[A-Za-z0-9 _-]{1,100}$");

    private ValidationUtil() {}

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) return false;
        String cleaned = phone.replaceAll("[\\s()-]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }

    public static boolean isValidName(String name) {
        if (isNullOrEmpty(name)) return false;
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeInteger(String value) {
        try {
            return Integer.parseInt(value.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveDouble(String value) {
        try {
            return Double.parseDouble(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeDouble(String value) {
        try {
            return Double.parseDouble(value.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean isValidAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr.trim());
            return age >= 0 && age <= 150;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidUsername(String username) {
        if (isNullOrEmpty(username)) return false;
        String u = username.trim();
        return u.length() >= 3 && u.length() <= 50 && u.matches("^[A-Za-z0-9._-]+$");
    }

    /**
     * Sanitize string to prevent XSS/SQL injection at display level.
     * Real SQL injection is prevented via prepared statements in DAOs.
     */
    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim()
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static String requireNonEmpty(String value, String fieldName) {
        if (isNullOrEmpty(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        return value.trim();
    }
}
