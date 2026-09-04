package com.sunrisedental.dao.impl;

import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test against the real local MySQL database — the clash
 * detection SQL in {@code hasClash()} is the riskiest hand-written query
 * in the data access tier (decision 24's test 1 of 3), so it gets a real
 * test against real rows rather than only unit-level trust.
 *
 * <p>{@code users}/{@code dentists} fixture rows are inserted with plain
 * JDBC in {@link #setUp()} rather than through {@code UserDAOImpl}, since
 * {@code UserDAOImpl.save()} is still a stub (see DESIGN.md decision 25's
 * note on the User-hierarchy insert strategy). Everything this test
 * creates is deleted again in {@link #tearDown()}.</p>
 */
class AppointmentDAOImplTest {

    private final AppointmentDAOImpl appointmentDAO = new AppointmentDAOImpl();
    private final PatientDAOImpl patientDAO = new PatientDAOImpl();
    private final TreatmentDAOImpl treatmentDAO = new TreatmentDAOImpl();

    private int dentistUserId;
    private int dentistId;
    private Patient patient;
    private Treatment treatment;
    private final LocalDate testDate = LocalDate.now().plusYears(1); // far enough out to never collide with real data

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "INSERT INTO users (username, password, full_name, role) "
                    + "VALUES ('test.dentist.dao', 'x', 'Test Dentist DAO', 'DENTIST')");
            dentistUserId = lastInsertId(conn);
            stmt.executeUpdate(
                    "INSERT INTO dentists (dentist_id, daily_appointment_limit) VALUES (" + dentistUserId + ", 20)");
            dentistId = dentistUserId;
        }

        patient = patientDAO.save(new Patient(0, "Test Patient DAO", "1 Test Rd", "0770000000"));
        treatment = treatmentDAO.save(new Treatment(0, "Test Cleaning DAO", 3000.0, 30));
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM appointments WHERE dentist_id = " + dentistId);
            stmt.executeUpdate("DELETE FROM dentists WHERE dentist_id = " + dentistId);
            stmt.executeUpdate("DELETE FROM users WHERE user_id = " + dentistUserId);
            stmt.executeUpdate("DELETE FROM patients WHERE patient_id = " + patient.getPatientId());
            stmt.executeUpdate("DELETE FROM treatments WHERE treatment_id = " + treatment.getTreatmentId());
        }
    }

    private int lastInsertId(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Appointment bookExistingAppointment(LocalTime time) {
        Appointment appointment = new Appointment(0, patient, dentistOf(), treatment, testDate, time);
        return appointmentDAO.save(appointment);
    }

    private com.sunrisedental.model.Dentist dentistOf() {
        com.sunrisedental.model.Dentist dentist = new com.sunrisedental.model.Dentist();
        dentist.setDentistId(dentistId);
        return dentist;
    }

    @Test
    void hasClash_trueWhenRequestedWindowOverlapsAnExistingAppointment() {
        bookExistingAppointment(LocalTime.of(10, 0)); // occupies 10:00-10:30 (treatment is 30 min)

        assertTrue(appointmentDAO.hasClash(dentistId, testDate, LocalTime.of(10, 15), 30),
                "starts inside the existing appointment");
        assertTrue(appointmentDAO.hasClash(dentistId, testDate, LocalTime.of(9, 45), 20),
                "ends inside the existing appointment (9:45-10:05 overlaps 10:00-10:30)");
    }

    @Test
    void hasClash_falseWhenRequestedWindowIsAdjacentOrDisjoint() {
        bookExistingAppointment(LocalTime.of(10, 0)); // occupies 10:00-10:30

        assertFalse(appointmentDAO.hasClash(dentistId, testDate, LocalTime.of(10, 30), 30),
                "starts exactly when the existing appointment ends — back-to-back, not overlapping");
        assertFalse(appointmentDAO.hasClash(dentistId, testDate, LocalTime.of(9, 0), 30),
                "ends exactly when the existing appointment starts");
        assertFalse(appointmentDAO.hasClash(dentistId, testDate.plusDays(1), LocalTime.of(10, 15), 30),
                "same time, different day");
    }

    @Test
    void hasClash_ignoresCancelledAppointments() {
        Appointment appointment = bookExistingAppointment(LocalTime.of(10, 0));
        appointment.cancel(com.sunrisedental.model.ChangeReason.PATIENT);
        appointmentDAO.update(appointment);

        assertFalse(appointmentDAO.hasClash(dentistId, testDate, LocalTime.of(10, 15), 30));
    }

    @Test
    void save_thenFindByAppointmentNumber_returnsAFullyPopulatedAppointment() {
        Appointment saved = bookExistingAppointment(LocalTime.of(11, 0));

        Optional<Appointment> found = appointmentDAO.findByAppointmentNumber(saved.getAppointmentNumber());

        assertTrue(found.isPresent());
        assertEquals(patient.getPatientId(), found.get().getPatient().getPatientId());
        assertEquals(treatment.getTreatmentId(), found.get().getTreatment().getTreatmentId());
        assertEquals(dentistId, found.get().getDentist().getDentistId());
        assertEquals(AppointmentStatus.SCHEDULED, found.get().getStatus());
    }

    @Test
    void findByDentistAndDate_returnsOnlyThatDentistsAppointmentsOnThatDate() {
        bookExistingAppointment(LocalTime.of(9, 0));
        bookExistingAppointment(LocalTime.of(14, 0));

        List<Appointment> schedule = appointmentDAO.findByDentistAndDate(dentistId, testDate);

        assertEquals(2, schedule.size());
    }
}
