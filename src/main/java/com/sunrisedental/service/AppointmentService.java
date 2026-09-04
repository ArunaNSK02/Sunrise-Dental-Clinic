package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.dao.PatientDAO;
import com.sunrisedental.dao.impl.AppointmentDAOImpl;
import com.sunrisedental.dao.impl.DentistDAOImpl;
import com.sunrisedental.dao.impl.PatientDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.model.ChangeReason;
import com.sunrisedental.model.DelayDecision;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Business tier for Register New Appointment and Search Appointment,
 * matching sequence diagram 3.2. {@link #checkDentistAvailability} is the
 * {@code <<include>>} from the use case diagram made concrete: the three
 * tests from decision 24, run in cheapest-first order so an unavailable
 * dentist never triggers the clash query at all —
 * <ol>
 *   <li>the requested moment isn't inside one of the dentist's own
 *       {@code AvailabilityBlock}s (in-memory, on the {@link Dentist}
 *       object already loaded by the caller);</li>
 *   <li>the dentist isn't already at their daily appointment limit
 *       ({@link DentistDAO#countAppointmentsOnDate});</li>
 *   <li>no existing appointment clashes with the requested
 *       [time, time + treatment duration) window
 *       ({@link AppointmentDAO#hasClash}).</li>
 * </ol>
 */
public class AppointmentService {

    private final PatientDAO patientDAO;
    private final DentistDAO dentistDAO;
    private final AppointmentDAO appointmentDAO;

    public AppointmentService() {
        this(new PatientDAOImpl(), new DentistDAOImpl(), new AppointmentDAOImpl());
    }

    public AppointmentService(PatientDAO patientDAO, DentistDAO dentistDAO, AppointmentDAO appointmentDAO) {
        this.patientDAO = patientDAO;
        this.dentistDAO = dentistDAO;
        this.appointmentDAO = appointmentDAO;
    }

    public boolean checkDentistAvailability(Dentist dentist, LocalDate date, LocalTime time, Treatment treatment) {
        return checkDentistAvailability(dentist, date, time, treatment, 0);
    }

    /**
     * Same three-test check, but excluding one appointment from both the
     * daily-limit count and the clash test — used by
     * {@link #rescheduleAppointment} so moving an appointment doesn't
     * spuriously fail against its own still-present old slot. {@code 0}
     * (never a real appointment number) means "exclude nothing", which is
     * what the 4-arg overload above passes for a fresh booking.
     */
    public boolean checkDentistAvailability(Dentist dentist, LocalDate date, LocalTime time, Treatment treatment,
                                             int excludeAppointmentNumber) {
        if (!dentist.isAvailable(LocalDateTime.of(date, time))) {
            return false;
        }
        int existingCount = dentistDAO.countAppointmentsOnDate(dentist.getDentistId(), date, excludeAppointmentNumber);
        if (existingCount >= dentist.getDailyAppointmentLimit()) {
            return false;
        }
        return !appointmentDAO.hasClash(
                dentist.getDentistId(), date, time, treatment.getDurationMinutes(), excludeAppointmentNumber);
    }

    /**
     * Find-or-register step from sequence diagram 3.2's {@code opt}
     * fragment — Register New Patient's {@code <<extend>>} (decision 3),
     * made concrete: only inserts a new patient row when no existing
     * patient matches the given contact number.
     */
    public Patient findOrRegisterPatient(String name, String address, String contactNumber) {
        return patientDAO.findByContactNumber(contactNumber)
                .orElseGet(() -> patientDAO.save(new Patient(0, name, address, contactNumber)));
    }

    /**
     * Registers a new appointment once availability has already been
     * confirmed by the caller (the servlet calls
     * {@link #checkDentistAvailability} first so it can show a proper
     * "unavailable" response instead of an exception reaching the user —
     * see sequence diagram 3.2's outer {@code alt}). This method
     * re-checks anyway before persisting, since availability could have
     * changed between the check and this call (e.g. another receptionist
     * booked the same slot).
     */
    public Appointment registerAppointment(Patient patient, Dentist dentist, Treatment treatment,
                                            LocalDate date, LocalTime time) {
        if (!checkDentistAvailability(dentist, date, time, treatment)) {
            throw new IllegalStateException(
                    "Dentist " + dentist.getFullName() + " is not available on " + date + " at " + time);
        }
        return appointmentDAO.save(new Appointment(0, patient, dentist, treatment, date, time));
    }

    public Optional<Appointment> searchAppointment(int appointmentNumber) {
        return appointmentDAO.findByAppointmentNumber(appointmentNumber);
    }

    /**
     * Cancel Appointment (use case, decision 7): one action regardless of
     * cause — {@code reason} is just an attribute recorded on the record,
     * not a different code path. {@code <<include>>}s Search Appointment
     * on the use case diagram; here that's just
     * {@link #findAppointmentOrThrow}.
     */
    public Appointment cancelAppointment(int appointmentNumber, ChangeReason reason) {
        Appointment appointment = findAppointmentOrThrow(appointmentNumber);
        appointment.cancel(reason);
        appointmentDAO.update(appointment);
        return appointment;
    }

    /**
     * Record Appointment Delay, dentist-caused branch (decision 10,
     * sequence diagram 3.3): no decision step — cascades automatically to
     * every one of that dentist's remaining non-cancelled appointments
     * that day, starting from (and including) the appointment the dentist
     * reported the delay against. The dentist is the constrained
     * resource; everything after the delay point necessarily shifts.
     *
     * @return the appointments that were actually delayed, for the caller
     *         to display back to the receptionist/dentist
     */
    public List<Appointment> recordDentistDelay(int triggeringAppointmentNumber, int delayMinutes) {
        Appointment trigger = findAppointmentOrThrow(triggeringAppointmentNumber);
        int dentistId = trigger.getDentist().getDentistId();

        List<Appointment> affected = new ArrayList<>();
        for (Appointment appointment : appointmentDAO.findByDentistAndDate(dentistId, trigger.getDate())) {
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }
            if (appointment.getTime().isBefore(trigger.getTime())) {
                continue;
            }
            appointment.recordDentistDelay(delayMinutes);
            appointmentDAO.update(appointment);
            affected.add(appointment);
        }
        return affected;
    }

    /**
     * Record Appointment Delay, patient-caused branch (decision 11,
     * sequence diagram 3.3): a manual Wait/Skip decision, recorded on
     * this one appointment via {@link Appointment#recordPatientDelay}.
     * <b>WAIT</b> then cascades to the rest of that dentist's day exactly
     * like {@link #recordDentistDelay} does — the dentist is now running
     * behind regardless of why. <b>SKIP</b> cancels this slot (handled
     * inside {@code Appointment.recordPatientDelay} itself) and stops
     * there; handing the patient to {@link #rescheduleAppointment} is a
     * separate, explicit follow-up action by the receptionist — decision
     * 23 already models this as two HTTP round trips, not one blocking
     * call, so this method doesn't reschedule automatically.
     */
    public Appointment recordPatientDelay(int appointmentNumber, int delayMinutes, DelayDecision decision) {
        Appointment appointment = findAppointmentOrThrow(appointmentNumber);
        appointment.recordPatientDelay(delayMinutes, decision);
        appointmentDAO.update(appointment);

        if (decision == DelayDecision.WAIT) {
            int dentistId = appointment.getDentist().getDentistId();
            for (Appointment other : appointmentDAO.findByDentistAndDate(dentistId, appointment.getDate())) {
                if (other.getAppointmentNumber() == appointment.getAppointmentNumber()) {
                    continue; // already updated above, with the WAIT/SKIP-aware method
                }
                if (other.getStatus() == AppointmentStatus.CANCELLED) {
                    continue;
                }
                if (other.getTime().isBefore(appointment.getTime())) {
                    continue;
                }
                other.recordDentistDelay(delayMinutes);
                appointmentDAO.update(other);
            }
        }
        return appointment;
    }

    /**
     * Reschedule Appointment (decision 12): changes an existing
     * appointment's date/time rather than allocating a new appointment
     * number — reached either directly, or as the follow-up action after
     * a patient-delay SKIP decision (the {@code <<extend>>} on the use
     * case diagram, extension point "patient skipped").
     *
     * <p>Re-runs the full three-test availability check against the
     * <i>new</i> slot, excluding this appointment itself (decisions from
     * entries 61-63) so its own current slot never counts against it.
     * Deliberately re-fetches the dentist via {@link DentistDAO#findById}
     * rather than trusting {@code appointment.getDentist()} — the
     * {@code Dentist} object {@code AppointmentDAOImpl} attaches to an
     * {@code Appointment} comes from a join against {@code users}/
     * {@code dentists} only, with no {@code availability_blocks} join, so
     * it would silently report every slot as available (test 1 of 3 would
     * never actually run). Caught during development, not from a bug
     * report — worth the extra query to get right.</p>
     */
    public Appointment rescheduleAppointment(int appointmentNumber, LocalDate newDate, LocalTime newTime) {
        Appointment appointment = findAppointmentOrThrow(appointmentNumber);
        Dentist dentist = dentistDAO.findById(appointment.getDentist().getDentistId())
                .orElseThrow(() -> new NoSuchElementException(
                        "No dentist with id " + appointment.getDentist().getDentistId()));

        if (!checkDentistAvailability(dentist, newDate, newTime, appointment.getTreatment(), appointmentNumber)) {
            throw new IllegalStateException(
                    "Dr. " + dentist.getFullName() + " is not available on " + newDate + " at " + newTime);
        }

        appointment.reschedule(newDate, newTime);
        appointmentDAO.update(appointment);
        return appointment;
    }

    private Appointment findAppointmentOrThrow(int appointmentNumber) {
        return appointmentDAO.findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new NoSuchElementException("No appointment with number " + appointmentNumber));
    }
}
