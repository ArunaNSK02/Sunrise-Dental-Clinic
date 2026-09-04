package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.dao.PatientDAO;
import com.sunrisedental.dao.impl.AppointmentDAOImpl;
import com.sunrisedental.dao.impl.DentistDAOImpl;
import com.sunrisedental.dao.impl.PatientDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        if (!dentist.isAvailable(LocalDateTime.of(date, time))) {
            return false;
        }
        int existingCount = dentistDAO.countAppointmentsOnDate(dentist.getDentistId(), date);
        if (existingCount >= dentist.getDailyAppointmentLimit()) {
            return false;
        }
        return !appointmentDAO.hasClash(dentist.getDentistId(), date, time, treatment.getDurationMinutes());
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
}
