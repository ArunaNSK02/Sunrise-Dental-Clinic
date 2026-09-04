package com.sunrisedental.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A dentist (class diagram, docs/DESIGN.md). Extends {@link User} directly
 * — not {@code Receptionist} — mirroring the use case diagram, where
 * Dentist is a separate actor with schedule/self-service permissions, not
 * a Receptionist specialisation (decision 15).
 *
 * <p>{@code dailyAppointmentLimit} and {@code unavailablePeriods} back the
 * Set Daily Appointment Limit / Set Availability use cases (decision 13);
 * {@link #isAvailable} is the model-level building block for
 * {@code AppointmentService.checkDentistAvailability()}'s three-test check
 * (decision 24 / sequence diagram 3.2) — the real double-booking clash
 * test against other appointments lives in the service+DAO tier, since it
 * needs to query the database, not just this object's own state.</p>
 */
public class Dentist extends User {

    private int dentistId;
    private int dailyAppointmentLimit;
    private List<AvailabilityBlock> unavailablePeriods = new ArrayList<>();

    public Dentist() {
        super();
    }

    public Dentist(int userId, String username, String password, String fullName,
                    int dentistId, int dailyAppointmentLimit) {
        super(userId, username, password, fullName);
        this.dentistId = dentistId;
        this.dailyAppointmentLimit = dailyAppointmentLimit;
    }

    /**
     * Placeholder matching the class diagram's method signature. The real
     * implementation is a data-tier lookup ({@code AppointmentDAO.findByDentistAndDate})
     * — this model class doesn't cache appointments itself.
     */
    public List<Appointment> viewSchedule(java.time.LocalDate date) {
        return new ArrayList<>();
    }

    public void setDailyAppointmentLimit(int limit) {
        this.dailyAppointmentLimit = limit;
    }

    public void setAvailability(AvailabilityBlock block) {
        unavailablePeriods.add(block);
    }

    /**
     * Availability-block half of the three-test availability check
     * (docs/DESIGN.md decision 24, test 2 of 3). Does not check the daily
     * limit (test 3) or existing-appointment clashes (test 1) — both of
     * those need data this object doesn't hold, and belong in
     * {@code AppointmentService}.
     */
    public boolean isAvailable(LocalDateTime dateTime) {
        return unavailablePeriods.stream().noneMatch(block -> block.covers(dateTime));
    }

    public int getDentistId() {
        return dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public int getDailyAppointmentLimit() {
        return dailyAppointmentLimit;
    }

    public List<AvailabilityBlock> getUnavailablePeriods() {
        return unavailablePeriods;
    }

    public void setUnavailablePeriods(List<AvailabilityBlock> unavailablePeriods) {
        this.unavailablePeriods = unavailablePeriods;
    }
}
