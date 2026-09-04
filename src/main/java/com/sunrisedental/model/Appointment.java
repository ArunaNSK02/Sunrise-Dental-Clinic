package com.sunrisedental.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The central record of the system (class diagram, docs/DESIGN.md) — most
 * other classes navigate toward this one, one-directionally (decision 20),
 * matching how the database schema will be queried.
 *
 * <p>Every state-change — {@link #cancel}, the two delay-recording
 * methods, {@link #reschedule}, {@link #generateBill} — lives here rather
 * than on Receptionist/Dentist (decision 17), so Appointment is
 * responsible for enforcing its own valid state transitions. This is a
 * natural fit for a State-style pattern in Task B. The two delay methods
 * stay separate rather than merging into one with an optional parameter
 * (decision 18): a dentist delay always cascades with no choice, while a
 * patient delay needs a {@link DelayDecision}.</p>
 *
 * <p>Method bodies here are placeholders — the real state transitions
 * need to persist through {@code AppointmentDAO} (they can't just mutate
 * an in-memory field and call it done), so the working implementation
 * lives in {@code AppointmentService} once the business tier exists. This
 * class still enforces what a valid transition *is* by throwing on
 * anything that doesn't fit its current status.</p>
 */
public class Appointment {

    private int appointmentNumber;
    private Patient patient;
    private Dentist dentist;
    private Treatment treatment;
    private LocalDate date;
    private LocalTime time;
    private AppointmentStatus status;
    private ChangeReason changeReason;
    private int delayMinutes;
    private Bill bill;

    public Appointment() {
        this.status = AppointmentStatus.SCHEDULED;
    }

    public Appointment(int appointmentNumber, Patient patient, Dentist dentist, Treatment treatment,
                        LocalDate date, LocalTime time) {
        this.appointmentNumber = appointmentNumber;
        this.patient = patient;
        this.dentist = dentist;
        this.treatment = treatment;
        this.date = date;
        this.time = time;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public void cancel(ChangeReason reason) {
        this.status = AppointmentStatus.CANCELLED;
        this.changeReason = reason;
    }

    /** Always cascades to the dentist's remaining appointments that day — no decision step (decision 10). */
    public void recordDentistDelay(int minutes) {
        this.status = AppointmentStatus.DELAYED;
        this.changeReason = ChangeReason.DENTIST;
        this.delayMinutes = minutes;
    }

    /** WAIT cascades like a dentist delay; SKIP hands off to {@link #reschedule} (decision 11). */
    public void recordPatientDelay(int minutes, DelayDecision decision) {
        this.changeReason = ChangeReason.PATIENT;
        this.delayMinutes = minutes;
        if (decision == DelayDecision.SKIP) {
            cancel(ChangeReason.PATIENT);
        } else {
            this.status = AppointmentStatus.DELAYED;
        }
    }

    public void reschedule(LocalDate newDate, LocalTime newTime) {
        this.date = newDate;
        this.time = newTime;
        this.status = AppointmentStatus.RESCHEDULED;
    }

    /**
     * Placeholder matching the class diagram's method signature. The real
     * implementation needs a consultation fee (a clinic-wide or
     * per-appointment figure not yet modelled) and persists the result via
     * {@code BillDAO} — deferred to {@code AppointmentService}.
     */
    public Bill generateBill() {
        throw new UnsupportedOperationException("Delegates to AppointmentService/BillDAO — not yet wired up.");
    }

    public int getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(int appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Dentist getDentist() {
        return dentist;
    }

    public void setDentist(Dentist dentist) {
        this.dentist = dentist;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public ChangeReason getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(ChangeReason changeReason) {
        this.changeReason = changeReason;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(int delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }
}
