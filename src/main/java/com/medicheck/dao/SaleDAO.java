package com.medicheck.dao;

import com.medicheck.model.Sale;
import com.medicheck.model.SaleItem;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data access for billing/sales with transactional processing.
 */
public class SaleDAO extends BaseDAO {

    public List<Sale> findAll() {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name AS cashier_name FROM sales s " +
                     "JOIN users u ON s.cashier_id = u.id ORDER BY s.created_at DESC LIMIT 500";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll sales failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public List<Sale> findByDateRange(LocalDate from, LocalDate to) {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name AS cashier_name FROM sales s " +
                     "JOIN users u ON s.cashier_id = u.id " +
                     "WHERE DATE(s.created_at) BETWEEN ? AND ? ORDER BY s.created_at DESC";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findByDateRange failed", e);
        } finally {
            release(conn);
        }
        return list;
    }

    public Optional<Sale> findByInvoiceNo(String invoiceNo) {
        String sql = "SELECT s.*, u.full_name AS cashier_name FROM sales s " +
                     "JOIN users u ON s.cashier_id = u.id WHERE s.invoice_no=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, invoiceNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            log.error("findByInvoiceNo failed: {}", invoiceNo, e);
        } finally {
            release(conn);
        }
        return Optional.empty();
    }

    /**
     * Save sale and items in a single transaction. Deducts medicine quantities.
     */
    public boolean saveSaleWithItems(Sale sale, List<SaleItem> items) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Insert sale
            String saleSql = "INSERT INTO sales (invoice_no, patient_id, patient_name, cashier_id, prescription_id, subtotal, tax_rate, tax_amount, discount, total, payment_method, payment_status, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement salePs = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            salePs.setString(1, sale.getInvoiceNo());
            if (sale.getPatientId() != null) salePs.setInt(2, sale.getPatientId());
            else salePs.setNull(2, Types.INTEGER);
            salePs.setString(3, sale.getPatientName());
            salePs.setInt(4, sale.getCashierId());
            if (sale.getPrescriptionId() != null) salePs.setInt(5, sale.getPrescriptionId());
            else salePs.setNull(5, Types.INTEGER);
            salePs.setBigDecimal(6, sale.getSubtotal());
            salePs.setBigDecimal(7, sale.getTaxRate());
            salePs.setBigDecimal(8, sale.getTaxAmount());
            salePs.setBigDecimal(9, sale.getDiscount());
            salePs.setBigDecimal(10, sale.getTotal());
            salePs.setString(11, sale.getPaymentMethod());
            salePs.setString(12, sale.getPaymentStatus());
            salePs.setString(13, sale.getNotes());
            salePs.executeUpdate();

            ResultSet keys = salePs.getGeneratedKeys();
            if (!keys.next()) throw new SQLException("Sale insert failed - no generated key");
            int saleId = keys.getInt(1);
            sale.setId(saleId);

            // Insert items and update inventory
            String itemSql = "INSERT INTO sale_items (sale_id, medicine_id, medicine_name, quantity, unit_price, total_price) VALUES (?,?,?,?,?,?)";
            String invSql = "UPDATE medicines SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
            String logSql = "INSERT INTO inventory_logs (medicine_id, medicine_name, action, quantity_before, quantity_change, quantity_after, notes, user_id) " +
                            "SELECT id, name, 'SALE', quantity, ?, quantity - ?, ?, ? FROM medicines WHERE id = ?";

            PreparedStatement itemPs = conn.prepareStatement(itemSql);
            PreparedStatement invPs = conn.prepareStatement(invSql);
            PreparedStatement logPs = conn.prepareStatement(logSql);

            for (SaleItem item : items) {
                itemPs.setInt(1, saleId);
                itemPs.setInt(2, item.getMedicineId());
                itemPs.setString(3, item.getMedicineName());
                itemPs.setInt(4, item.getQuantity());
                itemPs.setBigDecimal(5, item.getUnitPrice());
                itemPs.setBigDecimal(6, item.getTotalPrice());
                itemPs.addBatch();

                invPs.setInt(1, item.getQuantity());
                invPs.setInt(2, item.getMedicineId());
                invPs.setInt(3, item.getQuantity());
                invPs.addBatch();

                logPs.setInt(1, -item.getQuantity());
                logPs.setInt(2, item.getQuantity());
                logPs.setString(3, "Sale invoice: " + sale.getInvoiceNo());
                logPs.setInt(4, sale.getCashierId());
                logPs.setInt(5, item.getMedicineId());
                logPs.addBatch();
            }

            itemPs.executeBatch();
            int[] invResults = invPs.executeBatch();
            logPs.executeBatch();

            // Verify all inventory updates succeeded
            for (int result : invResults) {
                if (result == 0) throw new SQLException("Insufficient stock for one or more medicines");
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            log.error("saveSaleWithItems failed: {}", e.getMessage(), e);
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
        } finally {
            release(conn);
        }
        return false;
    }

    public List<SaleItem> findItemsBySaleId(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM sale_items WHERE sale_id=?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, saleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SaleItem item = new SaleItem();
                item.setId(rs.getInt("id"));
                item.setSaleId(rs.getInt("sale_id"));
                item.setMedicineId(rs.getInt("medicine_id"));
                item.setMedicineName(rs.getString("medicine_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setTotalPrice(rs.getBigDecimal("total_price"));
                items.add(item);
            }
        } catch (SQLException e) {
            log.error("findItemsBySaleId {} failed", saleId, e);
        } finally {
            release(conn);
        }
        return items;
    }

    public BigDecimal getTodaySales() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM sales WHERE DATE(created_at) = CURDATE() AND payment_status='Paid'";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            log.error("getTodaySales failed", e);
        } finally {
            release(conn);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getMonthRevenue(int year, int month) {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM sales WHERE YEAR(created_at)=? AND MONTH(created_at)=? AND payment_status='Paid'";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, year); ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            log.error("getMonthRevenue failed", e);
        } finally {
            release(conn);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Returns daily totals: date -> total for a date range.
     */
    public Map<String, BigDecimal> getDailySalesTrend(int days) {
        Map<String, BigDecimal> trend = new LinkedHashMap<>();
        String sql = "SELECT DATE(created_at) AS sale_date, SUM(total) AS day_total " +
                     "FROM sales WHERE DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL ? DAY) AND payment_status='Paid' " +
                     "GROUP BY DATE(created_at) ORDER BY sale_date";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                trend.put(rs.getString("sale_date"), rs.getBigDecimal("day_total"));
            }
        } catch (SQLException e) {
            log.error("getDailySalesTrend failed", e);
        } finally {
            release(conn);
        }
        return trend;
    }

    /**
     * Returns top N selling medicines: medicine_name -> total_qty.
     */
    public Map<String, Integer> getTopSellingMedicines(int limit) {
        Map<String, Integer> topMeds = new LinkedHashMap<>();
        String sql = "SELECT medicine_name, SUM(quantity) AS total_qty FROM sale_items " +
                     "GROUP BY medicine_name ORDER BY total_qty DESC LIMIT ?";
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                topMeds.put(rs.getString("medicine_name"), rs.getInt("total_qty"));
            }
        } catch (SQLException e) {
            log.error("getTopSellingMedicines failed", e);
        } finally {
            release(conn);
        }
        return topMeds;
    }

    public long getLastInvoiceNumber() {
        String sql = "SELECT MAX(CAST(SUBSTRING_INDEX(invoice_no, '-', -1) AS UNSIGNED)) FROM sales";
        Connection conn = null;
        try {
            conn = getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            log.error("getLastInvoiceNumber failed", e);
        } finally {
            release(conn);
        }
        return 0;
    }

    private Sale mapRow(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setId(rs.getInt("id"));
        s.setInvoiceNo(rs.getString("invoice_no"));
        int patientId = rs.getInt("patient_id");
        if (!rs.wasNull()) s.setPatientId(patientId);
        s.setPatientName(rs.getString("patient_name"));
        s.setCashierId(rs.getInt("cashier_id"));
        s.setCashierName(rs.getString("cashier_name"));
        s.setSubtotal(rs.getBigDecimal("subtotal"));
        s.setTaxRate(rs.getBigDecimal("tax_rate"));
        s.setTaxAmount(rs.getBigDecimal("tax_amount"));
        s.setDiscount(rs.getBigDecimal("discount"));
        s.setTotal(rs.getBigDecimal("total"));
        s.setPaymentMethod(rs.getString("payment_method"));
        s.setPaymentStatus(rs.getString("payment_status"));
        s.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) s.setCreatedAt(created.toLocalDateTime());
        return s;
    }
}
