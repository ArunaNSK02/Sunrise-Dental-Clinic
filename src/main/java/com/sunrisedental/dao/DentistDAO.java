package com.sunrisedental.dao;

import com.sunrisedental.model.AvailabilityBlock;
import com.sunrisedental.model.Dentist;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Dentist-specific queries beyond the shared credential lookups already
 * covered by {@link UserDAO} — capacity/availability configuration
 * (Set Daily Appointment Limit / Set Availability use cases, decision 13)
 * and the appointment-count check that Check Dentist Availability's third
 * test needs (decision 24).
 */
public interface DentistDAO {

    Optional<Dentist> findById(int dentistId);

    List<Dentist> findAll();

    void updateDailyAppointmentLimit(int dentistId, int limit);

    void addAvailabilityBlock(int dentistId, AvailabilityBlock block);

    List<AvailabilityBlock> findAvailabilityBlocks(int dentistId);

    /**
     * How many appointments this dentist already has on the given date —
     * feeds the daily-limit check. {@code excludeAppointmentNumber}: pass
     * {@code 0} for a fresh booking, or the appointment being moved for
     * Reschedule Appointment — a same-day time change shouldn't count
     * against the limit as if it were a brand new appointment (see
     * {@link AppointmentDAO#hasClash} for the matching reasoning on the
     * clash test).
     */
    int countAppointmentsOnDate(int dentistId, LocalDate date, int excludeAppointmentNumber);
}
