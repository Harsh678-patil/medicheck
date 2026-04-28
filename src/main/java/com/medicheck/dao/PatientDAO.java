package com.medicheck.dao;

import com.medicheck.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for Patient entity.
 */
public class PatientDAO extends BaseDAO {

    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT p.*, d.full_name AS doctor_name FROM patients p " +
                     "LEFT JOIN doctors d ON p.doctor_id = d.id ORDER BY p.full_name";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll patients failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public List<Patient> search(String keyword) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT p.*, d.full_name AS doctor_name FROM patients p " +
                     "LEFT JOIN doctors d ON p.doctor_id = d.id " +
                     "WHERE p.full_name LIKE ? OR p.phone LIKE ? OR p.disease LIKE ? ORDER BY p.full_name";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("search patients failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public Optional<Patient> findById(int id) {
        String sql = "SELECT p.*, d.full_name AS doctor_name FROM patients p " +
                     "LEFT JOIN doctors d ON p.doctor_id = d.id WHERE p.id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            log.error("findById patient {} failed", id, e);
        } finally {
            release(conn);
        }
        return Optional.empty();
    }

    public boolean insert(Patient p) {
        String sql = "INSERT INTO patients (full_name, age, gender, phone, email, address, blood_group, disease, allergies, doctor_id, emergency_contact, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getFullName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getAddress());
            ps.setString(7, p.getBloodGroup());
            ps.setString(8, p.getDisease());
            ps.setString(9, p.getAllergies());
            if (p.getDoctorId() > 0) ps.setInt(10, p.getDoctorId());
            else ps.setNull(10, Types.INTEGER);
            ps.setString(11, p.getEmergencyContact());
            ps.setString(12, p.getNotes());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) p.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            log.error("insert patient failed: {}", p.getFullName(), e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean update(Patient p) {
        String sql = "UPDATE patients SET full_name=?, age=?, gender=?, phone=?, email=?, address=?, blood_group=?, disease=?, allergies=?, doctor_id=?, emergency_contact=?, notes=? WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, p.getFullName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getAddress());
            ps.setString(7, p.getBloodGroup());
            ps.setString(8, p.getDisease());
            ps.setString(9, p.getAllergies());
            if (p.getDoctorId() > 0) ps.setInt(10, p.getDoctorId());
            else ps.setNull(10, Types.INTEGER);
            ps.setString(11, p.getEmergencyContact());
            ps.setString(12, p.getNotes());
            ps.setInt(13, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("update patient failed: {}", p.getId(), e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM patients WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete patient {} failed", id, e);
        } finally {
            release(conn);
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM patients";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("count patients failed", e);
        } finally {
            release(conn);
        }
        return 0;
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setFullName(rs.getString("full_name"));
        p.setAge(rs.getInt("age"));
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setDisease(rs.getString("disease"));
        p.setAllergies(rs.getString("allergies"));
        p.setDoctorId(rs.getInt("doctor_id"));
        p.setDoctorName(rs.getString("doctor_name"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        p.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) p.setCreatedAt(created.toLocalDateTime());
        return p;
    }
}
