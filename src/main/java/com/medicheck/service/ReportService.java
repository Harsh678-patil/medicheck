package com.medicheck.service;

import com.medicheck.config.AppConfig;
import com.medicheck.dao.MedicineDAO;
import com.medicheck.dao.SaleDAO;
import com.medicheck.model.Medicine;
import com.medicheck.model.Sale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Report generation service for analytics and export.
 */
public class ReportService {

    private final SaleDAO saleDAO = new SaleDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    /**
     * Monthly revenue data for the last N months.
     * Returns: month label -> total revenue
     */
    public Map<String, BigDecimal> getMonthlyRevenue(int months) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            String label = d.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + d.getYear();
            BigDecimal revenue = saleDAO.getMonthRevenue(d.getYear(), d.getMonthValue());
            result.put(label, revenue);
        }
        return result;
    }

    public Map<String, BigDecimal> getDailySalesTrend(int days) {
        return saleDAO.getDailySalesTrend(days);
    }

    public Map<String, Integer> getTopSellingMedicines(int limit) {
        return saleDAO.getTopSellingMedicines(limit);
    }

    public List<Medicine> getLowStockReport() {
        return medicineDAO.findLowStock();
    }

    public List<Medicine> getExpiredMedicines() {
        return medicineDAO.findExpiringSoon(0).stream()
                .filter(Medicine::isExpired)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Medicine> getExpiringSoon(int days) {
        return medicineDAO.findExpiringSoon(days);
    }

    public List<Sale> getDailySalesReport(LocalDate date) {
        return saleDAO.findByDateRange(date, date);
    }

    public List<Sale> getDateRangeSales(LocalDate from, LocalDate to) {
        return saleDAO.findByDateRange(from, to);
    }

    /**
     * Summary stats for dashboard cards.
     */
    public Map<String, Object> getDashboardStats(MedicineService medicineService, PatientService patientService, DoctorService doctorService) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPatients", patientService.getTotalCount());
        stats.put("totalDoctors", doctorService.getTotalCount());
        stats.put("totalMedicines", medicineService.getTotalCount());
        stats.put("lowStockCount", medicineService.getLowStockCount());
        stats.put("todaySales", saleDAO.getTodaySales());
        stats.put("monthRevenue", saleDAO.getMonthRevenue(LocalDate.now().getYear(), LocalDate.now().getMonthValue()));
        return stats;
    }
}
