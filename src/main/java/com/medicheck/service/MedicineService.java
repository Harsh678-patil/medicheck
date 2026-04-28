package com.medicheck.service;

import com.medicheck.dao.MedicineDAO;
import com.medicheck.model.Medicine;
import com.medicheck.util.AppLogger;
import com.medicheck.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class MedicineService {

    private final MedicineDAO medicineDAO = new MedicineDAO();

    public List<Medicine> getAllMedicines() {
        return medicineDAO.findAll();
    }

    public List<Medicine> searchMedicines(String keyword) {
        if (ValidationUtil.isNullOrEmpty(keyword)) return getAllMedicines();
        return medicineDAO.search(keyword.trim());
    }

    public Optional<Medicine> getById(int id) {
        return medicineDAO.findById(id);
    }

    public Optional<Medicine> findByBarcode(String barcode) {
        if (ValidationUtil.isNullOrEmpty(barcode)) return Optional.empty();
        return medicineDAO.findByBarcode(barcode.trim());
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineDAO.findLowStock();
    }

    public List<Medicine> getExpiringSoon(int daysAhead) {
        return medicineDAO.findExpiringSoon(daysAhead);
    }

    public Medicine save(Medicine medicine) {
        validate(medicine);
        boolean success;
        if (medicine.getId() == 0) {
            medicine.setActive(true);
            success = medicineDAO.insert(medicine);
            if (success) AppLogger.audit("CREATE_MEDICINE", "Medicine", medicine.getId(), "Added: " + medicine.getName());
        } else {
            success = medicineDAO.update(medicine);
            if (success) AppLogger.audit("UPDATE_MEDICINE", "Medicine", medicine.getId(), "Updated: " + medicine.getName());
        }
        if (!success) throw new RuntimeException("Failed to save medicine record");
        return medicine;
    }

    public void adjustStock(int medicineId, int newQuantity, String reason) {
        Optional<Medicine> mOpt = medicineDAO.findById(medicineId);
        if (mOpt.isEmpty()) throw new IllegalArgumentException("Medicine not found");
        if (newQuantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        medicineDAO.updateQuantity(medicineId, newQuantity);
        AppLogger.audit("ADJUST_STOCK", "Medicine", medicineId,
                "Stock adjusted to " + newQuantity + " | Reason: " + reason);
    }

    public void delete(int id) {
        Optional<Medicine> m = medicineDAO.findById(id);
        if (medicineDAO.softDelete(id)) {
            AppLogger.audit("DELETE_MEDICINE", "Medicine", id, "Removed: " + m.map(Medicine::getName).orElse("unknown"));
        } else {
            throw new RuntimeException("Failed to remove medicine");
        }
    }

    public int getTotalCount() {
        return medicineDAO.count();
    }

    public int getLowStockCount() {
        return medicineDAO.countLowStock();
    }

    private void validate(Medicine m) {
        if (ValidationUtil.isNullOrEmpty(m.getName())) throw new IllegalArgumentException("Medicine name is required");
        if (m.getPrice() == null || m.getPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        if (m.getQuantity() < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (m.getReorderLevel() < 0) throw new IllegalArgumentException("Reorder level cannot be negative");
        if (m.getExpiryDate() != null && m.getExpiryDate().isBefore(java.time.LocalDate.now()))
            throw new IllegalArgumentException("Warning: Expiry date is in the past");
    }
}
