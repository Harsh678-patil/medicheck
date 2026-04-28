package com.medicheck.service;

import com.medicheck.dao.PrescriptionDAO;
import com.medicheck.model.Prescription;
import com.medicheck.model.PrescriptionItem;
import com.medicheck.util.AppLogger;
import com.medicheck.util.InvoiceUtil;
import com.medicheck.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class PrescriptionService {

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    public List<Prescription> getAllPrescriptions() {
        return prescriptionDAO.findAll();
    }

    public List<Prescription> getByPatient(int patientId) {
        return prescriptionDAO.findByPatient(patientId);
    }

    public Optional<Prescription> getById(int id) {
        return prescriptionDAO.findById(id);
    }

    public List<PrescriptionItem> getItems(int prescriptionId) {
        return prescriptionDAO.findItemsByPrescription(prescriptionId);
    }

    public Prescription create(Prescription prescription) {
        if (prescription.getPatientId() <= 0) throw new IllegalArgumentException("Patient is required");
        if (prescription.getDoctorId() <= 0) throw new IllegalArgumentException("Doctor is required");
        if (ValidationUtil.isNullOrEmpty(prescription.getDisease())) throw new IllegalArgumentException("Diagnosis is required");
        if (prescription.getItems() == null || prescription.getItems().isEmpty())
            throw new IllegalArgumentException("At least one medicine must be prescribed");

        prescription.setPrescriptionNo(InvoiceUtil.generatePrescriptionNumber());
        prescription.setStatus("Active");

        boolean success = prescriptionDAO.insert(prescription);
        if (!success) throw new RuntimeException("Failed to save prescription");

        AppLogger.audit("CREATE_PRESCRIPTION", "Prescription", prescription.getId(),
                "Rx " + prescription.getPrescriptionNo() + " for patient ID " + prescription.getPatientId());
        return prescription;
    }

    public boolean markDispensed(int id) {
        boolean updated = prescriptionDAO.updateStatus(id, "Dispensed");
        if (updated) AppLogger.audit("DISPENSE_PRESCRIPTION", "Prescription", id, "Marked as dispensed");
        return updated;
    }
}
