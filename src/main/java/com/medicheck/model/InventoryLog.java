package com.medicheck.model;

import java.time.LocalDateTime;

/**
 * Tracks inventory changes (restocks, sales, adjustments).
 */
public class InventoryLog {
    private int id;
    private int medicineId;
    private String medicineName;
    private String action;  // RESTOCK, SALE, ADJUSTMENT, EXPIRED, DAMAGED
    private int quantityBefore;
    private int quantityChange;
    private int quantityAfter;
    private String notes;
    private int userId;
    private String username;
    private LocalDateTime createdAt;

    public InventoryLog() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public int getQuantityBefore() { return quantityBefore; }
    public void setQuantityBefore(int quantityBefore) { this.quantityBefore = quantityBefore; }
    public int getQuantityChange() { return quantityChange; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }
    public int getQuantityAfter() { return quantityAfter; }
    public void setQuantityAfter(int quantityAfter) { this.quantityAfter = quantityAfter; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
