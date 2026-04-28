package com.medicheck.model;

/**
 * Represents a single medicine line item in a prescription.
 */
public class PrescriptionItem {
    private int id;
    private int prescriptionId;
    private int medicineId;
    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;
    private int quantity;
    private String notes;

    public PrescriptionItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(int prescriptionId) { this.prescriptionId = prescriptionId; }
    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
