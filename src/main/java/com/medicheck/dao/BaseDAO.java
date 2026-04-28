package com.medicheck.dao;

import com.medicheck.config.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base DAO providing connection management helpers.
 * All DAOs extend this to acquire/release DB connections.
 */
public abstract class BaseDAO {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final DBConnection dbConnection = DBConnection.getInstance();

    protected Connection getConnection() throws SQLException {
        return dbConnection.getConnection();
    }

    protected void release(Connection conn) {
        dbConnection.releaseConnection(conn);
    }

    /**
     * Safely close resources without throwing.
     */
    protected void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try { r.close(); } catch (Exception ignored) {}
            }
        }
    }
}
