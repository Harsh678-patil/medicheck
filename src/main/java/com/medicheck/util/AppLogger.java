package com.medicheck.util;

import com.medicheck.dao.AuditLogDAO;
import com.medicheck.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized audit logger for security-critical events.
 * Writes to both SLF4J logging and the database audit_logs table.
 */
public final class AppLogger {

    private static final Logger log = LoggerFactory.getLogger(AppLogger.class);
    private static final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private AppLogger() {}

    public static void audit(String action, String entityType, int entityId, String details) {
        String username = SessionManager.getCurrentUsername();
        int userId = SessionManager.getCurrentUserId();

        log.info("[AUDIT] User={} | Action={} | Entity={} | ID={} | Details={}",
                username, action, entityType, entityId, details);

        try {
            AuditLog entry = new AuditLog();
            entry.setUserId(userId);
            entry.setUsername(username);
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setDetails(details);
            auditLogDAO.insert(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log to DB", e);
        }
    }

    public static void audit(String action, String details) {
        audit(action, null, 0, details);
    }

    public static void security(String message) {
        log.warn("[SECURITY] {}", message);
    }

    public static void info(String message) {
        log.info(message);
    }

    public static void warn(String message) {
        log.warn(message);
    }

    public static void error(String message, Throwable t) {
        log.error(message, t);
    }
}
