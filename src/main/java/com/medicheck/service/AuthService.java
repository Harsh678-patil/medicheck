package com.medicheck.service;

import com.medicheck.dao.UserDAO;
import com.medicheck.model.User;
import com.medicheck.util.AppLogger;
import com.medicheck.util.PasswordUtil;
import com.medicheck.util.SessionManager;
import com.medicheck.util.ValidationUtil;

import java.util.Optional;

/**
 * Handles authentication, session management, and password operations.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Authenticate a user by username and password.
     * Returns the User if successful, empty if failed.
     */
    public Optional<User> login(String username, String password) {
        if (ValidationUtil.isNullOrEmpty(username) || ValidationUtil.isNullOrEmpty(password)) {
            AppLogger.security("Login attempt with empty credentials");
            return Optional.empty();
        }

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            AppLogger.security("Login failed - unknown user: " + username);
            return Optional.empty();
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            AppLogger.security("Login attempt by deactivated user: " + username);
            return Optional.empty();
        }

        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            AppLogger.security("Invalid password for user: " + username);
            return Optional.empty();
        }

        // Start session
        SessionManager.login(user);
        userDAO.updateLastLogin(user.getId());
        AppLogger.audit("LOGIN", "User", user.getId(), "User logged in: " + username);
        return Optional.of(user);
    }

    public void logout() {
        if (SessionManager.isLoggedIn()) {
            AppLogger.audit("LOGOUT", "User", SessionManager.getCurrentUserId(),
                    "User logged out: " + SessionManager.getCurrentUsername());
        }
        SessionManager.logout();
    }

    /**
     * Register a new user. Admin only operation (enforced at UI layer).
     */
    public boolean register(User user, String plainPassword) {
        if (ValidationUtil.isNullOrEmpty(user.getUsername())) {
            throw new IllegalArgumentException("Username is required");
        }
        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username must be 3-50 characters, letters/numbers/._- only");
        }
        if (userDAO.usernameExists(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (!PasswordUtil.isStrong(plainPassword)) {
            throw new IllegalArgumentException("Password must be min 8 chars with uppercase, lowercase, digit, and special character");
        }
        user.setPasswordHash(PasswordUtil.hash(plainPassword));
        user.setActive(true);
        boolean created = userDAO.insert(user);
        if (created) {
            AppLogger.audit("CREATE_USER", "User", user.getId(), "New user created: " + user.getUsername());
        }
        return created;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        if (!PasswordUtil.verify(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!PasswordUtil.isStrong(newPassword)) {
            throw new IllegalArgumentException("New password does not meet strength requirements");
        }
        boolean updated = userDAO.updatePassword(userId, PasswordUtil.hash(newPassword));
        if (updated) {
            AppLogger.audit("CHANGE_PASSWORD", "User", userId, "Password changed for: " + user.getUsername());
        }
        return updated;
    }

    public boolean resetPassword(int userId, String newPassword) {
        if (!SessionManager.isAdmin()) {
            throw new SecurityException("Only admin can reset passwords");
        }
        if (!PasswordUtil.isStrong(newPassword)) {
            throw new IllegalArgumentException("Password does not meet strength requirements");
        }
        boolean updated = userDAO.updatePassword(userId, PasswordUtil.hash(newPassword));
        if (updated) {
            AppLogger.audit("RESET_PASSWORD", "User", userId, "Password reset by admin: " + SessionManager.getCurrentUsername());
        }
        return updated;
    }
}
