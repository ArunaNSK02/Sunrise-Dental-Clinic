package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * The busiest DAO in the system — Appointment is the central record most
 * other classes navigate toward (decision 20), and this interface backs
 * Register New Appointment, Search Appointment, Cancel/Delay/Reschedule,
 * and the availability clash check (sequence diagrams 3.1-3.3).
 */
public interface AppointmentDAO {

    Optional<Appointment> findByAppointmentNumber(int appointmentNumber);

    List<Appointment> findByPatientId(int patientId);

    List<Appointment> findByDentistAndDate(int dentistId, LocalDate date);

    /**
     * Existing-appointment clash test — test 1 of the three-test
     * availability check (decision 24). {@code durationMinutes} is the
     * requested treatment's slot length, used to compute the occupied
     * time window rather than treating every appointment as a single
     * instant.
     */
    boolean hasClash(int dentistId, LocalDate date, LocalTime time, int durationMinutes);

    Appointment save(Appointment appointment);

    void update(Appointment appointment);
}
