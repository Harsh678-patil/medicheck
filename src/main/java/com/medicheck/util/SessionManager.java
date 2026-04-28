package com.medicheck.util;

import com.medicheck.model.User;

import java.time.LocalDateTime;

/**
 * Thread-safe session manager for the currently logged-in user.
 * Handles session state, timeout, and role-based checks.
 */
public final class SessionManager {

    private static User currentUser;
    private static LocalDateTime sessionStart;
    private static int timeoutMinutes = 60;
    private static String sessionToken;

    private SessionManager() {}

    public static void login(User user) {
        currentUser = user;
        sessionStart = LocalDateTime.now();
        sessionToken = java.util.UUID.randomUUID().toString();
    }

    public static void logout() {
        currentUser = null;
        sessionStart = null;
        sessionToken = null;
    }

    public static boolean isLoggedIn() {
        if (currentUser == null) return false;
        if (isSessionExpired()) {
            logout();
            return false;
        }
        return true;
    }

    public static boolean isSessionExpired() {
        if (sessionStart == null) return true;
        return sessionStart.plusMinutes(timeoutMinutes).isBefore(LocalDateTime.now());
    }

    public static void refreshSession() {
        if (currentUser != null) {
            sessionStart = LocalDateTime.now();
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "unknown";
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : 0;
    }

    public static String getCurrentRole() {
        return currentUser != null ? currentUser.getRoleName() : "";
    }

    public static boolean hasRole(String roleName) {
        return currentUser != null && roleName.equalsIgnoreCase(currentUser.getRoleName());
    }

    public static boolean isAdmin() {
        return hasRole("Admin");
    }

    public static boolean isDoctor() {
        return hasRole("Doctor");
    }

    public static boolean isPharmacist() {
        return hasRole("Pharmacist");
    }

    public static boolean canAccess(String... allowedRoles) {
        if (currentUser == null) return false;
        for (String role : allowedRoles) {
            if (role.equalsIgnoreCase(currentUser.getRoleName())) return true;
        }
        return false;
    }

    public static void setTimeout(int minutes) {
        timeoutMinutes = minutes;
    }

    public static String getSessionToken() {
        return sessionToken;
    }

    public static LocalDateTime getSessionStart() {
        return sessionStart;
    }
}
