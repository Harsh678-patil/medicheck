package com.medicheck.dao;

import com.medicheck.model.AuditLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO extends BaseDAO {

    public void insert(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, details, ip_address) VALUES (?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            if (log.getUserId() > 0) ps.setInt(1, log.getUserId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, log.getUsername());
            ps.setString(3, log.getAction());
            ps.setString(4, log.getEntityType());
            if (log.getEntityId() > 0) ps.setInt(5, log.getEntityId());
            else ps.setNull(5, Types.INTEGER);
            ps.setString(6, log.getDetails());
            ps.setString(7, log.getIpAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            this.log.error("insert audit_log failed", e);
        } finally {
            release(conn);
        }
    }

    public List<AuditLog> findRecent(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT ?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            this.log.error("findRecent audit_logs failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public List<AuditLog> findByUser(int userId) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id=? ORDER BY created_at DESC LIMIT 200";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            this.log.error("findByUser audit_logs failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setUsername(rs.getString("username"));
        a.setAction(rs.getString("action"));
        a.setEntityType(rs.getString("entity_type"));
        a.setEntityId(rs.getInt("entity_id"));
        a.setDetails(rs.getString("details"));
        a.setIpAddress(rs.getString("ip_address"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) a.setCreatedAt(created.toLocalDateTime());
        return a;
    }
}
