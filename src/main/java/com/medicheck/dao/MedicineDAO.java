package com.medicheck.dao;

import com.medicheck.model.Medicine;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for Medicine inventory.
 */
public class MedicineDAO extends BaseDAO {

    public List<Medicine> findAll() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE is_active=1 ORDER BY name";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll medicines failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public List<Medicine> search(String keyword) {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE is_active=1 AND (name LIKE ? OR generic_name LIKE ? OR barcode LIKE ? OR category LIKE ?) ORDER BY name";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw); ps.setString(4, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("search medicines failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public Optional<Medicine> findById(int id) {
        String sql = "SELECT * FROM medicines WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            log.error("findById medicine {} failed", id, e);
        } finally {
            release(conn);
        }
        return Optional.empty();
    }

    public Optional<Medicine> findByBarcode(String barcode) {
        String sql = "SELECT * FROM medicines WHERE barcode=? AND is_active=1";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, barcode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            log.error("findByBarcode failed for: {}", barcode, e);
        } finally {
            release(conn);
        }
        return Optional.empty();
    }

    public List<Medicine> findLowStock() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE is_active=1 AND quantity <= reorder_level ORDER BY quantity ASC";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findLowStock failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public List<Medicine> findExpiringSoon(int daysAhead) {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE is_active=1 AND expiry_date IS NOT NULL " +
                     "AND expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) ORDER BY expiry_date";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, daysAhead);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findExpiringSoon failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public boolean insert(Medicine m) {
        String sql = "INSERT INTO medicines (name, generic_name, barcode, batch_no, category, price, cost_price, quantity, reorder_level, expiry_date, manufacturer, description, unit, image_path, is_active) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, m.getName());
            ps.setString(2, m.getGenericName());
            ps.setString(3, m.getBarcode());
            ps.setString(4, m.getBatchNo());
            ps.setString(5, m.getCategory());
            ps.setBigDecimal(6, m.getPrice());
            ps.setBigDecimal(7, m.getCostPrice());
            ps.setInt(8, m.getQuantity());
            ps.setInt(9, m.getReorderLevel());
            if (m.getExpiryDate() != null) ps.setDate(10, Date.valueOf(m.getExpiryDate()));
            else ps.setNull(10, Types.DATE);
            ps.setString(11, m.getManufacturer());
            ps.setString(12, m.getDescription());
            ps.setString(13, m.getUnit());
            ps.setString(14, m.getImagePath());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) m.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            log.error("insert medicine failed: {}", m.getName(), e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean update(Medicine m) {
        String sql = "UPDATE medicines SET name=?, generic_name=?, barcode=?, batch_no=?, category=?, price=?, cost_price=?, quantity=?, reorder_level=?, expiry_date=?, manufacturer=?, description=?, unit=?, image_path=? WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, m.getName());
            ps.setString(2, m.getGenericName());
            ps.setString(3, m.getBarcode());
            ps.setString(4, m.getBatchNo());
            ps.setString(5, m.getCategory());
            ps.setBigDecimal(6, m.getPrice());
            ps.setBigDecimal(7, m.getCostPrice());
            ps.setInt(8, m.getQuantity());
            ps.setInt(9, m.getReorderLevel());
            if (m.getExpiryDate() != null) ps.setDate(10, Date.valueOf(m.getExpiryDate()));
            else ps.setNull(10, Types.DATE);
            ps.setString(11, m.getManufacturer());
            ps.setString(12, m.getDescription());
            ps.setString(13, m.getUnit());
            ps.setString(14, m.getImagePath());
            ps.setInt(15, m.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("update medicine {} failed", m.getId(), e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean updateQuantity(int id, int newQuantity) {
        String sql = "UPDATE medicines SET quantity=? WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, newQuantity);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updateQuantity medicine {} failed", id, e);
        } finally {
            release(conn);
        }
        return false;
    }

    public boolean softDelete(int id) {
        String sql = "UPDATE medicines SET is_active=0 WHERE id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("softDelete medicine {} failed", id, e);
        } finally {
            release(conn);
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM medicines WHERE is_active=1";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("count medicines failed", e);
        } finally {
            release(conn);
        }
        return 0;
    }

    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM medicines WHERE is_active=1 AND quantity <= reorder_level";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("countLowStock failed", e);
        } finally {
            release(conn);
        }
        return 0;
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setGenericName(rs.getString("generic_name"));
        m.setBarcode(rs.getString("barcode"));
        m.setBatchNo(rs.getString("batch_no"));
        m.setCategory(rs.getString("category"));
        BigDecimal price = rs.getBigDecimal("price");
        m.setPrice(price != null ? price : BigDecimal.ZERO);
        BigDecimal costPrice = rs.getBigDecimal("cost_price");
        m.setCostPrice(costPrice != null ? costPrice : BigDecimal.ZERO);
        m.setQuantity(rs.getInt("quantity"));
        m.setReorderLevel(rs.getInt("reorder_level"));
        Date expiry = rs.getDate("expiry_date");
        if (expiry != null) m.setExpiryDate(expiry.toLocalDate());
        m.setManufacturer(rs.getString("manufacturer"));
        m.setDescription(rs.getString("description"));
        m.setUnit(rs.getString("unit"));
        m.setImagePath(rs.getString("image_path"));
        m.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) m.setCreatedAt(created.toLocalDateTime());
        return m;
    }
}
