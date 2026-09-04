package com.sunrisedental.dao;

import com.sunrisedental.model.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientDAO {

    Optional<Patient> findById(int patientId);

    /**
     * Used by Register New Appointment's find-or-register step (sequence
     * diagram 3.2) to decide whether Register New Patient's {@code <<extend>>}
     * fires.
     */
    Optional<Patient> findByContactNumber(String contactNumber);

    List<Patient> findAll();

    Patient save(Patient patient);
}
