package com.medicheck.model;

import java.math.BigDecimal;

/**
 * Represents a single medicine line item in a sale/billing transaction.
 */
public class SaleItem {
    private int id;
    private int saleId;
    private int medicineId;
    private String medicineName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public SaleItem() {
        this.unitPrice = BigDecimal.ZERO;
        this.totalPrice = BigDecimal.ZERO;
    }

    public SaleItem(int medicineId, String medicineName, int quantity, BigDecimal unitPrice) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
