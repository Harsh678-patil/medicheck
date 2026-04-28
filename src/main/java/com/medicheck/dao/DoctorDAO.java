package com.medicheck.dao;

import com.medicheck.model.Doctor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorDAO extends BaseDAO {

    public List<Doctor> findAll() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY full_name";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll doctors failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public List<Doctor> findActive() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE is_active=1 ORDER BY full_name";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findActive doctors failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public List<Doctor> search(String keyword) {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE full_name LIKE ? OR specialty LIKE ? OR phone LIKE ? ORDER BY full_name";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("search doctors failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public Optional<Doctor> findById(int id) {
        String sql = "SELECT * FROM doctors WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            log.error("findById doctor {} failed", id, e);
        } finally {
            release(conn);
        }
        return Optional.empty();
    }

    public boolean insert(Doctor d) {
        String sql = "INSERT INTO doctors (user_id, full_name, specialty, phone, email, license_no, address, qualification, experience_years, is_active) VALUES (?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (d.getUserId() != null) ps.setInt(1, d.getUserId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, d.getFullName());
            ps.setString(3, d.getSpecialty());
            ps.setString(4, d.getPhone());
            ps.setString(5, d.getEmail());
            ps.setString(6, d.getLicenseNo());
            ps.setString(7, d.getAddress());
            ps.setString(8, d.getQualification());
            ps.setInt(9, d.getExperienceYears());
            ps.setBoolean(10, d.isActive());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) d.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            log.error("insert doctor failed: {}", d.getFullName(), e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean update(Doctor d) {
        String sql = "UPDATE doctors SET full_name=?, specialty=?, phone=?, email=?, license_no=?, address=?, qualification=?, experience_years=?, is_active=? WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, d.getFullName());
            ps.setString(2, d.getSpecialty());
            ps.setString(3, d.getPhone());
            ps.setString(4, d.getEmail());
            ps.setString(5, d.getLicenseNo());
            ps.setString(6, d.getAddress());
            ps.setString(7, d.getQualification());
            ps.setInt(8, d.getExperienceYears());
            ps.setBoolean(9, d.isActive());
            ps.setInt(10, d.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("update doctor {} failed", d.getId(), e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM doctors WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete doctor {} failed", id, e);
        } finally {
            release(conn);
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM doctors WHERE is_active=1";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("count doctors failed", e);
        } finally {
            release(conn);
        }
        return 0;
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getInt("id"));
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) d.setUserId(userId);
        d.setFullName(rs.getString("full_name"));
        d.setSpecialty(rs.getString("specialty"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setLicenseNo(rs.getString("license_no"));
        d.setAddress(rs.getString("address"));
        d.setQualification(rs.getString("qualification"));
        d.setExperienceYears(rs.getInt("experience_years"));
        d.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) d.setCreatedAt(created.toLocalDateTime());
        return d;
    }
}
