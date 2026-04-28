package com.medicheck;

import com.medicheck.config.AppConfig;
import com.medicheck.config.DBConnection;
import com.medicheck.ui.LoginView;
import com.medicheck.util.AppLogger;
import com.medicheck.service.SaleService;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

/**
 * Entry point for MediCheck Application.
 */
public class Main {

    public static void main(String[] args) {
        AppLogger.info("Starting MediCheck System...");

        // ── 1. Validate DB connection at startup ───────────────────────────
        DBConnection db = DBConnection.getInstance();
        if (!db.testConnection()) {
            AppConfig config = AppConfig.getInstance();
            String dbUrl = config.get("db.url", "jdbc:mysql://localhost:3306/medicheck");
            String dbUser = config.get("db.username", "root");
            String dbPassword = config.get("db.password", "");
            String maskedPassword = dbPassword.isEmpty() ? "(empty)" : "(set)";
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                    "❌  Cannot connect to MySQL database!\n\n" +
                    "Please ensure:\n" +
                    "  1. XAMPP is running\n" +
                    "  2. MySQL service is started in XAMPP Control Panel\n" +
                    "  3. Database 'medicheck' has been created\n" +
                    "  4. sql/setup_all.sql has been imported in phpMyAdmin\n\n" +
                    "DB URL: " + dbUrl + "\n" +
                    "Username: " + dbUser + "  |  Password: " + maskedPassword,
                    "Database Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            });
            return;
        }
        AppLogger.info("Database connection established successfully.");

        // ── 2. Initialize invoice counter ──────────────────────────────────
        try {
            new SaleService().initInvoiceCounter();
        } catch (Exception e) {
            AppLogger.warn("Failed to initialize invoice counter: " + e.getMessage());
        }

        // ── 3. Setup FlatLaf Dark Theme ────────────────────────────────────
        try {
            FlatDarkLaf.setup();
        } catch (Exception ex) {
            AppLogger.error("Failed to initialize FlatDarkLaf theme", ex);
        }

        // ── 4. Shutdown hook ───────────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AppLogger.info("Shutting down MediCheck...");
            DBConnection.getInstance().destroyPool();
            AppConfig.getInstance().save();
        }));

        // ── 5. Launch Login UI ─────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}
