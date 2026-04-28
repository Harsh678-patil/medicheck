package com.medicheck.service;

import com.medicheck.config.AppConfig;
import com.medicheck.dao.SaleDAO;
import com.medicheck.model.Sale;
import com.medicheck.model.SaleItem;
import com.medicheck.util.AppLogger;
import com.medicheck.util.InvoiceUtil;
import com.medicheck.util.SessionManager;
import com.medicheck.util.ValidationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Billing/POS service. Handles cart calculation, sale saving, and revenue queries.
 */
public class SaleService {

    private final SaleDAO saleDAO = new SaleDAO();
    private final AppConfig config = AppConfig.getInstance();

    public static final String[] PAYMENT_METHODS = {"Cash", "Card", "UPI", "Insurance"};

    /**
     * Calculate sale totals from cart items and discount.
     */
    public Sale calculateTotals(List<SaleItem> cartItems, BigDecimal discountAmount) {
        Sale sale = new Sale();
        BigDecimal subtotal = cartItems.stream()
                .map(SaleItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sale.setSubtotal(subtotal);

        BigDecimal taxRate = new BigDecimal(config.get("tax_rate", "18.0"));
        sale.setTaxRate(taxRate);
        sale.setDiscount(discountAmount != null ? discountAmount : BigDecimal.ZERO);

        BigDecimal afterDiscount = subtotal.subtract(sale.getDiscount());
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) afterDiscount = BigDecimal.ZERO;

        BigDecimal taxAmount = afterDiscount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        sale.setTaxAmount(taxAmount);
        sale.setTotal(afterDiscount.add(taxAmount));
        return sale;
    }

    /**
     * Process and save a completed sale. Thread-safe via DB transaction.
     */
    public Sale processSale(Sale sale, List<SaleItem> items) {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Cart cannot be empty");
        if (sale.getTotal().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Sale total must be positive");

        sale.setInvoiceNo(InvoiceUtil.generateInvoiceNumber());
        sale.setCashierId(SessionManager.getCurrentUserId());

        boolean success = saleDAO.saveSaleWithItems(sale, items);
        if (!success) throw new RuntimeException("Failed to process sale - check stock availability");

        AppLogger.audit("CREATE_SALE", "Sale", sale.getId(),
                "Invoice: " + sale.getInvoiceNo() + " | Total: " + sale.getTotal());
        return sale;
    }

    public List<Sale> getAllSales() {
        return saleDAO.findAll();
    }

    public List<Sale> getSalesByDateRange(LocalDate from, LocalDate to) {
        return saleDAO.findByDateRange(from, to);
    }

    public Optional<Sale> findByInvoiceNo(String invoiceNo) {
        return saleDAO.findByInvoiceNo(invoiceNo);
    }

    public List<SaleItem> getSaleItems(int saleId) {
        return saleDAO.findItemsBySaleId(saleId);
    }

    public BigDecimal getTodaySales() {
        return saleDAO.getTodaySales();
    }

    public BigDecimal getMonthRevenue(int year, int month) {
        return saleDAO.getMonthRevenue(year, month);
    }

    public Map<String, BigDecimal> getDailySalesTrend(int days) {
        return saleDAO.getDailySalesTrend(days);
    }

    public Map<String, Integer> getTopSellingMedicines(int limit) {
        return saleDAO.getTopSellingMedicines(limit);
    }

    public void initInvoiceCounter() {
        long lastNum = saleDAO.getLastInvoiceNumber();
        InvoiceUtil.initInvoiceCounter(lastNum);
    }
}
