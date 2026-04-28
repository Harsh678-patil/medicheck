package com.medicheck.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Singleton for reading/writing application properties from app.properties.
 */
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static final String CONFIG_FILE = "app.properties";
    private static AppConfig instance;
    private final Properties properties = new Properties();

    private AppConfig() {
        load();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void load() {
        // Prefer a file next to the launched app so deployed installs stay configurable.
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                properties.load(is);
                log.info("Loaded app.properties from working directory");
                return;
            } catch (IOException e) {
                log.error("Failed to load app.properties from disk", e);
            }
        }
        // Fall back to the bundled classpath copy.
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                properties.load(is);
                log.info("Loaded app.properties from classpath");
                return;
            }
        } catch (IOException e) {
            log.debug("Could not load {} from classpath", CONFIG_FILE);
        }
        log.warn("app.properties not found. Using defaults.");
        loadDefaults();
    }

    private void loadDefaults() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/medicheck?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("db.pool.min", "2");
        properties.setProperty("db.pool.max", "10");
        properties.setProperty("app.name", "MediCheck");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("session.timeout.minutes", "60");
        properties.setProperty("tax_rate", "18.0");
        properties.setProperty("currency_symbol", "₹");
        properties.setProperty("openai.enabled", "false");
        properties.setProperty("images.upload.dir", "./uploads/medicines");
        properties.setProperty("reports.export.dir", "./exports");
    }

    public String get(String key) {
        return properties.getProperty(key, "");
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        return "true".equalsIgnoreCase(val.trim());
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public void save() {
        File file = new File(CONFIG_FILE);
        try (OutputStream os = new FileOutputStream(file)) {
            properties.store(os, "MediCheck Configuration");
            log.info("Saved app.properties");
        } catch (IOException e) {
            log.error("Failed to save app.properties", e);
        }
    }

    public Properties getProperties() {
        return properties;
    }
}
