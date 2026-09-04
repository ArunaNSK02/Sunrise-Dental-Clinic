package com.sunrisedental.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A patient of the clinic (class diagram, docs/DESIGN.md). Aggregated by
 * {@link Appointment} — a patient record and its appointments can each
 * outlive the other (decision 19), so this class does not hold a back
 * reference to every appointment ever booked; {@link #getAppointmentHistory()}
 * is served from the data access tier ({@code PatientDAO} /
 * {@code AppointmentDAO}), not from an in-memory list owned here.
 */
public class Patient {

    private int patientId;
    private String name;
    private String address;
    private String contactNumber;

    public Patient() {
    }

    public Patient(int patientId, String name, String address, String contactNumber) {
        this.patientId = patientId;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    /**
     * Placeholder matching the class diagram's method signature. The real
     * implementation is a data-tier lookup ({@code AppointmentDAO.findByPatientId})
     * — this model class deliberately doesn't cache the list itself.
     */
    public List<Appointment> getAppointmentHistory() {
        return new ArrayList<>();
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
