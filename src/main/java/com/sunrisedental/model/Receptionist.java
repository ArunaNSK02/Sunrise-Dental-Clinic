package com.sunrisedental.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Front-desk staff (class diagram, docs/DESIGN.md). Day-to-day operations:
 * registering appointments, searching for them, and reading the in-app
 * help section. {@link Administrator} extends this class to add
 * management-level operations (decision 15).
 *
 * <p>These methods are thin coordinators over the business tier — per
 * decision 17, Appointment owns all of its own state-changing behaviour,
 * so Receptionist never mutates an Appointment's fields directly. The
 * bodies here are placeholders; the real logic lives in
 * {@code com.sunrisedental.service.AppointmentService}, which these
 * methods will delegate to once the service tier exists.</p>
 */
public class Receptionist extends User {

    public Receptionist() {
        super();
    }

    public Receptionist(int userId, String username, String password, String fullName) {
        super(userId, username, password, fullName);
    }

    public Appointment registerAppointment(Patient patient, Dentist dentist, Treatment treatment,
                                            LocalDate date, LocalTime time) {
        throw new UnsupportedOperationException("Delegates to AppointmentService — not yet wired up.");
    }

    public Appointment searchAppointment(int appointmentNumber) {
        throw new UnsupportedOperationException("Delegates to AppointmentService — not yet wired up.");
    }

    public void viewHelp() {
        throw new UnsupportedOperationException("Delegates to a Help servlet — not yet wired up.");
    }
}
