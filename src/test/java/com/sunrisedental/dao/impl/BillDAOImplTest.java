package com.sunrisedental.dao.impl;

import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;
import com.sunrisedental.model.Dentist;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test proving {@code BillDAOImpl.save()} correctly uses the
 * {@code fn_bill_total} SQL function (schema.sql) to compute
 * {@code total_amount} — {@code bills.appointment_number} has a foreign
 * key to {@code appointments}, so a real appointment fixture (mirroring
 * {@code AppointmentDAOImplTest}'s pattern) is required, not just any
 * int.
 */
class BillDAOImplTest {

    private final BillDAOImpl billDAO = new BillDAOImpl();
    private final AppointmentDAOImpl appointmentDAO = new AppointmentDAOImpl();
    private final PatientDAOImpl patientDAO = new PatientDAOImpl();
    private final TreatmentDAOImpl treatmentDAO = new TreatmentDAOImpl();

    private int dentistUserId;
    private Patient patient;
    private Treatment treatment;
    private Appointment appointment;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO users (username, password, full_name, role) "
                    + "VALUES ('test.bill.dentist', 'x', 'Test Bill Dentist', 'DENTIST')");
            dentistUserId = lastInsertId(conn);
            stmt.executeUpdate("INSERT INTO dentists (dentist_id, daily_appointment_limit) VALUES ("
                    + dentistUserId + ", 20)");
        }
        patient = patientDAO.save(new Patient(0, "Test Bill Patient", "1 Test Rd", "0720000000"));
        treatment = treatmentDAO.save(new Treatment(0, "Test Bill Treatment", 6000.00, 30));

        Dentist dentist = new Dentist();
        dentist.setDentistId(dentistUserId);
        appointment = appointmentDAO.save(new Appointment(
                0, patient, dentist, treatment, LocalDate.now().plusYears(3), LocalTime.of(9, 0)));
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM bills WHERE appointment_number = " + appointment.getAppointmentNumber());
            stmt.executeUpdate("DELETE FROM appointments WHERE appointment_number = " + appointment.getAppointmentNumber());
            stmt.executeUpdate("DELETE FROM dentists WHERE dentist_id = " + dentistUserId);
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

    @Test
    void save_computesTotalAmountViaTheFnBillTotalFunction() {
        Bill bill = new Bill(0, 6000.00, 1000.00, LocalDate.now());

        billDAO.save(appointment.getAppointmentNumber(), bill);

        Optional<Bill> found = billDAO.findByAppointmentNumber(appointment.getAppointmentNumber());
        assertTrue(found.isPresent());
        assertEquals(7000.00, found.get().getTotalAmount(), 0.001);
    }
}
