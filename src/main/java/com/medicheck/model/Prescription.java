package com.medicheck.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a doctor's prescription for a patient.
 */
public class Prescription {
    private int id;
    private String prescriptionNo;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private String disease;
    private String symptoms;
    private String notes;
    private String status;
    private List<PrescriptionItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Prescription() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPrescriptionNo() { return prescriptionNo; }
    public void setPrescriptionNo(String prescriptionNo) { this.prescriptionNo = prescriptionNo; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDisease() { return disease; }
    public void setDisease(String disease) { this.disease = disease; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<PrescriptionItem> getItems() { return items; }
    public void setItems(List<PrescriptionItem> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() { return prescriptionNo; }
}
