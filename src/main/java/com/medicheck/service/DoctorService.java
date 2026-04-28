package com.medicheck.service;

import com.medicheck.dao.DoctorDAO;
import com.medicheck.model.Doctor;
import com.medicheck.util.AppLogger;
import com.medicheck.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class DoctorService {

    private final DoctorDAO doctorDAO = new DoctorDAO();

    public List<Doctor> getAllDoctors() {
        return doctorDAO.findAll();
    }

    public List<Doctor> getActiveDoctors() {
        return doctorDAO.findActive();
    }

    public List<Doctor> searchDoctors(String keyword) {
        if (ValidationUtil.isNullOrEmpty(keyword)) return getAllDoctors();
        return doctorDAO.search(keyword.trim());
    }

    public Optional<Doctor> getById(int id) {
        return doctorDAO.findById(id);
    }

    public Doctor save(Doctor doctor) {
        validate(doctor);
        boolean success;
        if (doctor.getId() == 0) {
            doctor.setActive(true);
            success = doctorDAO.insert(doctor);
            if (success) AppLogger.audit("CREATE_DOCTOR", "Doctor", doctor.getId(), "Added: " + doctor.getFullName());
        } else {
            success = doctorDAO.update(doctor);
            if (success) AppLogger.audit("UPDATE_DOCTOR", "Doctor", doctor.getId(), "Updated: " + doctor.getFullName());
        }
        if (!success) throw new RuntimeException("Failed to save doctor record");
        return doctor;
    }

    public void delete(int id) {
        Optional<Doctor> d = doctorDAO.findById(id);
        if (doctorDAO.delete(id)) {
            AppLogger.audit("DELETE_DOCTOR", "Doctor", id, "Deleted: " + d.map(Doctor::getFullName).orElse("unknown"));
        } else {
            throw new RuntimeException("Failed to delete doctor");
        }
    }

    public int getTotalCount() {
        return doctorDAO.count();
    }

    private void validate(Doctor d) {
        if (ValidationUtil.isNullOrEmpty(d.getFullName())) throw new IllegalArgumentException("Doctor name is required");
        if (!ValidationUtil.isNullOrEmpty(d.getPhone()) && !ValidationUtil.isValidPhone(d.getPhone()))
            throw new IllegalArgumentException("Invalid phone number");
        if (!ValidationUtil.isNullOrEmpty(d.getEmail()) && !ValidationUtil.isValidEmail(d.getEmail()))
            throw new IllegalArgumentException("Invalid email address");
    }
}
