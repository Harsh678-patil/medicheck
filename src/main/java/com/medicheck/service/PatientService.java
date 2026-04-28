package com.medicheck.service;

import com.medicheck.dao.PatientDAO;
import com.medicheck.model.Patient;
import com.medicheck.util.AppLogger;
import com.medicheck.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class PatientService {

    private final PatientDAO patientDAO = new PatientDAO();

    public List<Patient> getAllPatients() {
        return patientDAO.findAll();
    }

    public List<Patient> searchPatients(String keyword) {
        if (ValidationUtil.isNullOrEmpty(keyword)) return getAllPatients();
        return patientDAO.search(keyword.trim());
    }

    public Optional<Patient> getById(int id) {
        return patientDAO.findById(id);
    }

    public Patient save(Patient patient) {
        validate(patient);
        boolean success;
        if (patient.getId() == 0) {
            success = patientDAO.insert(patient);
            if (success) AppLogger.audit("CREATE_PATIENT", "Patient", patient.getId(), "Added: " + patient.getFullName());
        } else {
            success = patientDAO.update(patient);
            if (success) AppLogger.audit("UPDATE_PATIENT", "Patient", patient.getId(), "Updated: " + patient.getFullName());
        }
        if (!success) throw new RuntimeException("Failed to save patient record");
        return patient;
    }

    public void delete(int id) {
        Optional<Patient> p = patientDAO.findById(id);
        if (patientDAO.delete(id)) {
            AppLogger.audit("DELETE_PATIENT", "Patient", id, "Deleted: " + p.map(Patient::getFullName).orElse("unknown"));
        } else {
            throw new RuntimeException("Failed to delete patient");
        }
    }

    public int getTotalCount() {
        return patientDAO.count();
    }

    private void validate(Patient p) {
        if (ValidationUtil.isNullOrEmpty(p.getFullName())) throw new IllegalArgumentException("Patient name is required");
        if (p.getFullName().length() < 2 || p.getFullName().length() > 100) throw new IllegalArgumentException("Name must be 2-100 characters");
        if (p.getAge() < 0 || p.getAge() > 150) throw new IllegalArgumentException("Invalid age");
        if (!ValidationUtil.isNullOrEmpty(p.getPhone()) && !ValidationUtil.isValidPhone(p.getPhone()))
            throw new IllegalArgumentException("Invalid phone number format");
        if (!ValidationUtil.isNullOrEmpty(p.getEmail()) && !ValidationUtil.isValidEmail(p.getEmail()))
            throw new IllegalArgumentException("Invalid email format");
    }
}
