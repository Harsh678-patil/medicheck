package com.medicheck.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

/**
 * Thread-safe database connection pool.
 * Manages a pool of MySQL connections for the application.
 */
public class DBConnection {

    private static final Logger log = LoggerFactory.getLogger(DBConnection.class);

    private static DBConnection instance;
    private final Queue<Connection> pool = new LinkedList<>();
    private final String url;
    private final String username;
    private final String password;
    private final int minConnections;
    private final int maxConnections;
    private int totalConnections = 0;

    private DBConnection() {
        Properties props = AppConfig.getInstance().getProperties();
        this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/medicheck?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true");
        this.username = props.getProperty("db.username", "root");
        this.password = props.getProperty("db.password", "");
        this.minConnections = Integer.parseInt(props.getProperty("db.pool.min", "2"));
        this.maxConnections = Integer.parseInt(props.getProperty("db.pool.max", "10"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initPool();
        } catch (ClassNotFoundException e) {
            log.error("MySQL JDBC Driver not found", e);
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    private void initPool() {
        for (int i = 0; i < minConnections; i++) {
            try {
                pool.add(createConnection());
                totalConnections++;
            } catch (SQLException e) {
                log.error("Failed to create initial DB connection #{}", i + 1, e);
            }
        }
        log.info("DB pool initialized with {} connections", pool.size());
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Acquire a connection from the pool. Blocks briefly and creates new if pool is empty.
     */
    public synchronized Connection getConnection() throws SQLException {
        // Try to find a valid connection from pool
        while (!pool.isEmpty()) {
            Connection conn = pool.poll();
            try {
                if (conn != null && !conn.isClosed() && conn.isValid(2)) {
                    return conn;
                }
                totalConnections--;
            } catch (SQLException ignored) {
                totalConnections--;
            }
        }
        // Create new if under max
        if (totalConnections < maxConnections) {
            Connection conn = createConnection();
            totalConnections++;
            log.debug("Created new DB connection. Total: {}", totalConnections);
            return conn;
        }
        throw new SQLException("Connection pool exhausted. Max connections (" + maxConnections + ") reached.");
    }

    /**
     * Return a connection to the pool.
     */
    public synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.setAutoCommit(true);
                    pool.offer(conn);
                } else {
                    totalConnections--;
                }
            } catch (SQLException e) {
                log.warn("Error releasing connection: {}", e.getMessage());
                totalConnections--;
            }
        }
    }

    /**
     * Test if the database is reachable.
     */
    public boolean testConnection() {
        try (Connection conn = createConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            log.error("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Close all pooled connections on application shutdown.
     */
    public synchronized void destroyPool() {
        for (Connection conn : pool) {
            try {
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (SQLException ignored) {}
        }
        pool.clear();
        totalConnections = 0;
        log.info("DB connection pool destroyed");
    }
}
