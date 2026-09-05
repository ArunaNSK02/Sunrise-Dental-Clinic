package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.dao.PatientDAO;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.model.AvailabilityBlock;
import com.sunrisedental.model.ChangeReason;
import com.sunrisedental.model.DelayDecision;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AppointmentService}'s three-test availability
 * check (decision 24), find-or-register patient step (decision 3), and
 * the Cancel/Record Delay/Reschedule flows (decisions 7, 10-12, sequence
 * diagram 3.3) — exercised against hand-written DAO test doubles rather
 * than a real database. {@link com.sunrisedental.dao.impl.AppointmentDAOImplTest}
 * already covers the real SQL (including exclude-self behaviour); this
 * class is purely about the business rule composition.
 */
class AppointmentServiceTest {

    private final Patient patient = new Patient(1, "Jane Silva", "1 Galle Rd", "0771234567");
    private final Treatment treatment = new Treatment(1, "Filling", 4000.0, 30);
    private final LocalDate date = LocalDate.of(2026, 9, 10);
    private final LocalTime time = LocalTime.of(10, 0);

    private Dentist availableDentist(int dailyLimit) {
        return new Dentist(1, "d.perera", "x", "Dr. Perera", 1, dailyLimit);
    }

    private Appointment appointmentAt(int number, Dentist dentist, LocalTime at) {
        Appointment appointment = new Appointment(number, patient, dentist, treatment, date, at);
        return appointment;
    }

    @Test
    void checkDentistAvailability_falseWhenTimeFallsInsideAnUnavailablePeriod() {
        Dentist dentist = availableDentist(20);
        dentist.setAvailability(new AvailabilityBlock(1,
                LocalDateTime.of(date, LocalTime.of(9, 0)), LocalDateTime.of(date, LocalTime.of(11, 0)), "Lunch"));
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(0), new StubAppointmentDAO(false));

        assertFalse(service.checkDentistAvailability(dentist, date, time, treatment));
    }

    @Test
    void checkDentistAvailability_falseWhenDailyLimitAlreadyReached() {
        Dentist dentist = availableDentist(3);
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(3), new StubAppointmentDAO(false));

        assertFalse(service.checkDentistAvailability(dentist, date, time, treatment));
    }

    @Test
    void checkDentistAvailability_falseWhenAnExistingAppointmentClashes() {
        Dentist dentist = availableDentist(20);
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(0), new StubAppointmentDAO(true));

        assertFalse(service.checkDentistAvailability(dentist, date, time, treatment));
    }

    @Test
    void checkDentistAvailability_trueWhenAllThreeTestsPass() {
        Dentist dentist = availableDentist(20);
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(5), new StubAppointmentDAO(false));

        assertTrue(service.checkDentistAvailability(dentist, date, time, treatment));
    }

    @Test
    void findOrRegisterPatient_returnsExistingPatientWithoutSavingANewOne() {
        StubPatientDAO patientDAO = new StubPatientDAO();
        patientDAO.byContactNumber.put("0771234567", patient);
        AppointmentService service = new AppointmentService(
                patientDAO, new StubDentistDAO(0), new StubAppointmentDAO(false));

        Patient result = service.findOrRegisterPatient("Jane Silva", "1 Galle Rd", "0771234567");

        assertEquals(patient.getPatientId(), result.getPatientId());
        assertTrue(patientDAO.savedPatients.isEmpty());
    }

    @Test
    void findOrRegisterPatient_savesANewPatientWhenNoneMatches() {
        StubPatientDAO patientDAO = new StubPatientDAO();
        AppointmentService service = new AppointmentService(
                patientDAO, new StubDentistDAO(0), new StubAppointmentDAO(false));

        Patient result = service.findOrRegisterPatient("New Patient", "2 Kandy Rd", "0779999999");

        assertEquals("New Patient", result.getName());
        assertEquals(1, patientDAO.savedPatients.size());
    }

    @Test
    void registerAppointment_throwsWhenDentistIsNotAvailable() {
        Dentist dentist = availableDentist(3);
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(3), new StubAppointmentDAO(false));

        assertThrows(IllegalStateException.class,
                () -> service.registerAppointment(patient, dentist, treatment, date, time));
    }

    @Test
    void registerAppointment_savesWhenAvailable() {
        Dentist dentist = availableDentist(20);
        StubAppointmentDAO appointmentDAO = new StubAppointmentDAO(false);
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(0), appointmentDAO);

        Appointment result = service.registerAppointment(patient, dentist, treatment, date, time);

        assertEquals(1, appointmentDAO.savedAppointments.size());
        assertEquals(date, result.getDate());
    }

    @Test
    void cancelAppointment_setsStatusCancelledAndRecordsTheReason() {
        Dentist dentist = availableDentist(20);
        StubAppointmentDAO appointmentDAO = new StubAppointmentDAO(false);
        appointmentDAO.seed(appointmentAt(1, dentist, time));
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(0), appointmentDAO);

        Appointment result = service.cancelAppointment(1, ChangeReason.PATIENT);

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
        assertEquals(ChangeReason.PATIENT, result.getChangeReason());
        assertEquals(1, appointmentDAO.updatedAppointments.size());
    }

    @Test
    void recordDentistDelay_cascadesToRemainingAppointmentsButNotEarlierOrCancelledOnes() {
        Dentist dentist = availableDentist(20);
        StubAppointmentDAO appointmentDAO = new StubAppointmentDAO(false);
        Appointment earlier = appointmentAt(1, dentist, LocalTime.of(9, 0));   // before the trigger — unaffected
        Appointment trigger = appointmentAt(2, dentist, LocalTime.of(10, 0)); // the delay is reported here
        Appointment later = appointmentAt(3, dentist, LocalTime.of(11, 0));   // after — cascades
        Appointment cancelledLater = appointmentAt(4, dentist, LocalTime.of(12, 0));
        cancelledLater.cancel(ChangeReason.PATIENT); // cancelled — excluded from the cascade
        appointmentDAO.seed(earlier);
        appointmentDAO.seed(trigger);
        appointmentDAO.seed(later);
        appointmentDAO.seed(cancelledLater);
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(0), appointmentDAO);

        List<Appointment> affected = service.recordDentistDelay(2, 15);

        assertEquals(2, affected.size()); // trigger + later, not earlier or cancelledLater
        assertEquals(AppointmentStatus.SCHEDULED, earlier.getStatus());
        assertEquals(AppointmentStatus.DELAYED, trigger.getStatus());
        assertEquals(15, trigger.getDelayMinutes());
        assertEquals(AppointmentStatus.DELAYED, later.getStatus());
        assertEquals(15, later.getDelayMinutes());
    }

    @Test
    void recordPatientDelay_wait_cascadesToLaterAppointmentsForTheSameDentist() {
        Dentist dentist = availableDentist(20);
        StubAppointmentDAO appointmentDAO = new StubAppointmentDAO(false);
        Appointment delayed = appointmentAt(1, dentist, LocalTime.of(10, 0));
        Appointment later = appointmentAt(2, dentist, LocalTime.of(11, 0));
        appointmentDAO.seed(delayed);
        appointmentDAO.seed(later);
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(0), appointmentDAO);

        Appointment result = service.recordPatientDelay(1, 20, DelayDecision.WAIT);

        assertEquals(AppointmentStatus.DELAYED, result.getStatus());
        assertEquals(ChangeReason.PATIENT, result.getChangeReason());
        assertEquals(AppointmentStatus.DELAYED, later.getStatus()); // cascaded
        assertEquals(20, later.getDelayMinutes());
    }

    @Test
    void recordPatientDelay_skip_cancelsThisAppointmentAndDoesNotCascade() {
        Dentist dentist = availableDentist(20);
        StubAppointmentDAO appointmentDAO = new StubAppointmentDAO(false);
        Appointment skipped = appointmentAt(1, dentist, LocalTime.of(10, 0));
        Appointment later = appointmentAt(2, dentist, LocalTime.of(11, 0));
        appointmentDAO.seed(skipped);
        appointmentDAO.seed(later);
        AppointmentService service = new AppointmentService(
                new StubPatientDAO(), new StubDentistDAO(0), appointmentDAO);

        Appointment result = service.recordPatientDelay(1, 20, DelayDecision.SKIP);

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
        assertEquals(AppointmentStatus.SCHEDULED, later.getStatus()); // not touched
    }

    @Test
    void rescheduleAppointment_movesDateAndTimeWhenTheNewSlotIsAvailable() {
        Dentist dentist = availableDentist(20);
        StubAppointmentDAO appointmentDAO = new StubAppointmentDAO(false);
        appointmentDAO.seed(appointmentAt(1, dentist, time));
        StubDentistDAO dentistDAO = new StubDentistDAO(0);
        dentistDAO.dentistToReturn = dentist;
        AppointmentService service = new AppointmentService(new StubPatientDAO(), dentistDAO, appointmentDAO);

        LocalDate newDate = date.plusDays(1);
        LocalTime newTime = LocalTime.of(14, 0);
        Appointment result = service.rescheduleAppointment(1, newDate, newTime);

        assertEquals(AppointmentStatus.RESCHEDULED, result.getStatus());
        assertEquals(newDate, result.getDate());
        assertEquals(newTime, result.getTime());
        // The appointment being moved must be excluded from its own availability check.
        assertEquals(1, appointmentDAO.lastHasClashExclude);
        assertEquals(1, dentistDAO.lastCountExclude);
    }

    @Test
    void rescheduleAppointment_throwsWhenTheNewSlotIsUnavailable() {
        Dentist dentist = availableDentist(20);
        StubAppointmentDAO appointmentDAO = new StubAppointmentDAO(true); // every slot clashes
        appointmentDAO.seed(appointmentAt(1, dentist, time));
        StubDentistDAO dentistDAO = new StubDentistDAO(0);
        dentistDAO.dentistToReturn = dentist;
        AppointmentService service = new AppointmentService(new StubPatientDAO(), dentistDAO, appointmentDAO);

        assertThrows(IllegalStateException.class,
                () -> service.rescheduleAppointment(1, date.plusDays(1), LocalTime.of(14, 0)));
    }

    // --- Hand-written test doubles (no mocking framework) ---

    private static class StubPatientDAO implements PatientDAO {
        final Map<String, Patient> byContactNumber = new LinkedHashMap<>();
        final List<Patient> savedPatients = new ArrayList<>();

        @Override
        public Optional<Patient> findById(int patientId) {
            return Optional.empty();
        }

        @Override
        public Optional<Patient> findByContactNumber(String contactNumber) {
            return Optional.ofNullable(byContactNumber.get(contactNumber));
        }

        @Override
        public List<Patient> findAll() {
            return List.of();
        }

        @Override
        public Patient save(Patient patient) {
            patient.setPatientId(savedPatients.size() + 100);
            savedPatients.add(patient);
            return patient;
        }
    }

    private static class StubDentistDAO implements DentistDAO {
        private final int appointmentCountToReturn;
        Dentist dentistToReturn;
        Integer lastCountExclude;

        StubDentistDAO(int appointmentCountToReturn) {
            this.appointmentCountToReturn = appointmentCountToReturn;
        }

        @Override
        public Optional<Dentist> findById(int dentistId) {
            return Optional.ofNullable(dentistToReturn);
        }

        @Override
        public List<Dentist> findAll() {
            return List.of();
        }

        @Override
        public void updateDailyAppointmentLimit(int dentistId, int limit) {
        }

        @Override
        public void addAvailabilityBlock(int dentistId, AvailabilityBlock block) {
        }

        @Override
        public List<AvailabilityBlock> findAvailabilityBlocks(int dentistId) {
            return List.of();
        }

        @Override
        public int countAppointmentsOnDate(int dentistId, LocalDate date, int excludeAppointmentNumber) {
            lastCountExclude = excludeAppointmentNumber;
            return appointmentCountToReturn;
        }
    }

    /** Backed by a real in-memory map so findByAppointmentNumber/findByDentistAndDate/update behave truthfully. */
    private static class StubAppointmentDAO implements AppointmentDAO {
        private final boolean clash;
        final List<Appointment> savedAppointments = new ArrayList<>();
        final List<Appointment> updatedAppointments = new ArrayList<>();
        final Map<Integer, Appointment> byNumber = new LinkedHashMap<>();
        Integer lastHasClashExclude;

        StubAppointmentDAO(boolean clash) {
            this.clash = clash;
        }

        void seed(Appointment appointment) {
            byNumber.put(appointment.getAppointmentNumber(), appointment);
        }

        @Override
        public Optional<Appointment> findByAppointmentNumber(int appointmentNumber) {
            return Optional.ofNullable(byNumber.get(appointmentNumber));
        }

        @Override
        public List<Appointment> findByPatientId(int patientId) {
            return List.of();
        }

        @Override
        public List<Appointment> findByDentistAndDate(int dentistId, LocalDate date) {
            List<Appointment> result = new ArrayList<>();
            for (Appointment appointment : byNumber.values()) {
                if (appointment.getDentist().getDentistId() == dentistId && appointment.getDate().equals(date)) {
                    result.add(appointment);
                }
            }
            return result;
        }

        @Override
        public List<Appointment> findByDate(LocalDate date) {
            List<Appointment> result = new ArrayList<>();
            for (Appointment appointment : byNumber.values()) {
                if (appointment.getDate().equals(date)) {
                    result.add(appointment);
                }
            }
            return result;
        }

        @Override
        public boolean hasClash(int dentistId, LocalDate date, LocalTime time, int durationMinutes,
                                 int excludeAppointmentNumber) {
            lastHasClashExclude = excludeAppointmentNumber;
            return clash;
        }

        @Override
        public Appointment save(Appointment appointment) {
            appointment.setAppointmentNumber(savedAppointments.size() + 1);
            savedAppointments.add(appointment);
            byNumber.put(appointment.getAppointmentNumber(), appointment);
            return appointment;
        }

        @Override
        public void update(Appointment appointment) {
            updatedAppointments.add(appointment);
            byNumber.put(appointment.getAppointmentNumber(), appointment);
        }
    }
}
