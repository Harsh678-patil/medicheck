package com.medicheck.dao;

import com.medicheck.model.User;
import com.medicheck.model.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for User entity. Uses prepared statements throughout.
 */
public class UserDAO extends BaseDAO {

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT u.*, r.name AS role_name FROM users u " +
                     "JOIN roles r ON u.role_id = r.id WHERE u.username = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("findByUsername failed for: {}", username, e);
        } finally {
            release(conn);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT u.*, r.name AS role_name FROM users u " +
                     "JOIN roles r ON u.role_id = r.id WHERE u.id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            log.error("findById failed for id: {}", id, e);
        } finally {
            release(conn);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, r.name AS role_name FROM users u " +
                     "JOIN roles r ON u.role_id = r.id ORDER BY u.full_name";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll users failed", e);
        } finally {
            release(conn);
        }
        return users;
    }

    public boolean insert(User user) {
        String sql = "INSERT INTO users (username, full_name, email, password_hash, role_id, is_active) VALUES (?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setInt(5, user.getRoleId());
            ps.setBoolean(6, user.isActive());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) user.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            log.error("insert user failed: {}", user.getUsername(), e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET full_name=?, email=?, role_id=?, is_active=? WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getRoleId());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("update user failed: {}", user.getId(), e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newHashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updatePassword failed for userId: {}", userId, e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login=NOW() WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updateLastLogin failed", e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean setActive(int userId, boolean active) {
        String sql = "UPDATE users SET is_active=? WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("setActive failed for userId: {}", userId, e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            log.error("usernameExists check failed", e);
        } finally {
            release(conn);
        }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) u.setLastLogin(lastLogin.toLocalDateTime());
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) u.setCreatedAt(created.toLocalDateTime());
        return u;
    }
}
