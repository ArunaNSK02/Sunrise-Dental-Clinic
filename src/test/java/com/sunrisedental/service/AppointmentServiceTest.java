package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.dao.PatientDAO;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AvailabilityBlock;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AppointmentService}'s three-test availability
 * check (decision 24) and the find-or-register patient step (decision 3),
 * exercised against hand-written DAO test doubles rather than a real
 * database — {@link AppointmentDAOImplTest} in the dao.impl package
 * already covers the real SQL; this class is purely about the business
 * rule composition (which of the three tests fails, and in what order
 * they're checked).
 */
class AppointmentServiceTest {

    private final Patient patient = new Patient(1, "Jane Silva", "1 Galle Rd", "0771234567");
    private final Treatment treatment = new Treatment(1, "Filling", 4000.0, 30);
    private final LocalDate date = LocalDate.of(2026, 9, 10);
    private final LocalTime time = LocalTime.of(10, 0);

    private Dentist availableDentist(int dailyLimit) {
        Dentist dentist = new Dentist(1, "d.perera", "x", "Dr. Perera", 1, dailyLimit);
        return dentist;
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

    // --- Hand-written test doubles (no mocking framework) ---

    private static class StubPatientDAO implements PatientDAO {
        final java.util.Map<String, Patient> byContactNumber = new java.util.HashMap<>();
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

        StubDentistDAO(int appointmentCountToReturn) {
            this.appointmentCountToReturn = appointmentCountToReturn;
        }

        @Override
        public Optional<Dentist> findById(int dentistId) {
            return Optional.empty();
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
        public int countAppointmentsOnDate(int dentistId, LocalDate date) {
            return appointmentCountToReturn;
        }
    }

    private static class StubAppointmentDAO implements AppointmentDAO {
        private final boolean clash;
        final List<Appointment> savedAppointments = new ArrayList<>();

        StubAppointmentDAO(boolean clash) {
            this.clash = clash;
        }

        @Override
        public Optional<Appointment> findByAppointmentNumber(int appointmentNumber) {
            return Optional.empty();
        }

        @Override
        public List<Appointment> findByPatientId(int patientId) {
            return List.of();
        }

        @Override
        public List<Appointment> findByDentistAndDate(int dentistId, LocalDate date) {
            return List.of();
        }

        @Override
        public boolean hasClash(int dentistId, LocalDate date, LocalTime time, int durationMinutes) {
            return clash;
        }

        @Override
        public Appointment save(Appointment appointment) {
            appointment.setAppointmentNumber(savedAppointments.size() + 1);
            savedAppointments.add(appointment);
            return appointment;
        }

        @Override
        public void update(Appointment appointment) {
        }
    }
}
