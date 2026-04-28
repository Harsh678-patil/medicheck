package com.medicheck.dao;

import com.medicheck.model.Prescription;
import com.medicheck.model.PrescriptionItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrescriptionDAO extends BaseDAO {

    public List<Prescription> findAll() {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, pat.full_name AS patient_name, d.full_name AS doctor_name " +
                     "FROM prescriptions p " +
                     "JOIN patients pat ON p.patient_id = pat.id " +
                     "JOIN doctors d ON p.doctor_id = d.id " +
                     "ORDER BY p.created_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll prescriptions failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public List<Prescription> findByPatient(int patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, pat.full_name AS patient_name, d.full_name AS doctor_name " +
                     "FROM prescriptions p " +
                     "JOIN patients pat ON p.patient_id = pat.id " +
                     "JOIN doctors d ON p.doctor_id = d.id " +
                     "WHERE p.patient_id=? ORDER BY p.created_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findByPatient failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public Optional<Prescription> findById(int id) {
        String sql = "SELECT p.*, pat.full_name AS patient_name, d.full_name AS doctor_name " +
                     "FROM prescriptions p " +
                     "JOIN patients pat ON p.patient_id = pat.id " +
                     "JOIN doctors d ON p.doctor_id = d.id " +
                     "WHERE p.id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            log.error("findById prescription {} failed", id, e);
        } finally {
            release(conn);
        }
        return Optional.empty();
    }

    public boolean insert(Prescription p) {
        String sql = "INSERT INTO prescriptions (prescription_no, patient_id, doctor_id, disease, symptoms, notes, status) VALUES (?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getPrescriptionNo());
            ps.setInt(2, p.getPatientId());
            ps.setInt(3, p.getDoctorId());
            ps.setString(4, p.getDisease());
            ps.setString(5, p.getSymptoms());
            ps.setString(6, p.getNotes());
            ps.setString(7, p.getStatus() != null ? p.getStatus() : "Active");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                p.setId(keys.getInt(1));
                if (p.getItems() != null) {
                    insertItems(conn, p.getId(), p.getItems());
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            log.error("insert prescription failed", e);
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
        } finally {
            release(conn);
        }
        return false;
    }

    private void insertItems(Connection conn, int prescriptionId, List<PrescriptionItem> items) throws SQLException {
        String sql = "INSERT INTO prescription_items (prescription_id, medicine_id, dosage, frequency, duration, quantity, notes) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (PrescriptionItem item : items) {
            ps.setInt(1, prescriptionId);
            ps.setInt(2, item.getMedicineId());
            ps.setString(3, item.getDosage());
            ps.setString(4, item.getFrequency());
            ps.setString(5, item.getDuration());
            ps.setInt(6, item.getQuantity());
            ps.setString(7, item.getNotes());
            ps.addBatch();
        }
        ps.executeBatch();
    }

    public List<PrescriptionItem> findItemsByPrescription(int prescriptionId) {
        List<PrescriptionItem> items = new ArrayList<>();
        String sql = "SELECT pi.*, m.name AS medicine_name FROM prescription_items pi " +
                     "JOIN medicines m ON pi.medicine_id = m.id WHERE pi.prescription_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, prescriptionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setId(rs.getInt("id"));
                item.setPrescriptionId(prescriptionId);
                item.setMedicineId(rs.getInt("medicine_id"));
                item.setMedicineName(rs.getString("medicine_name"));
                item.setDosage(rs.getString("dosage"));
                item.setFrequency(rs.getString("frequency"));
                item.setDuration(rs.getString("duration"));
                item.setQuantity(rs.getInt("quantity"));
                item.setNotes(rs.getString("notes"));
                items.add(item);
            }
        } catch (SQLException e) {
            log.error("findItemsByPrescription {} failed", prescriptionId, e);
        } finally {
            release(conn);
        }
        return items;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE prescriptions SET status=? WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updateStatus prescription {} failed", id, e);
        } finally {
            release(conn);
        }
        return false;
    }

    private Prescription mapRow(ResultSet rs) throws SQLException {
        Prescription p = new Prescription();
        p.setId(rs.getInt("id"));
        p.setPrescriptionNo(rs.getString("prescription_no"));
        p.setPatientId(rs.getInt("patient_id"));
        p.setPatientName(rs.getString("patient_name"));
        p.setDoctorId(rs.getInt("doctor_id"));
        p.setDoctorName(rs.getString("doctor_name"));
        p.setDisease(rs.getString("disease"));
        p.setSymptoms(rs.getString("symptoms"));
        p.setNotes(rs.getString("notes"));
        p.setStatus(rs.getString("status"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) p.setCreatedAt(created.toLocalDateTime());
        return p;
    }
}
