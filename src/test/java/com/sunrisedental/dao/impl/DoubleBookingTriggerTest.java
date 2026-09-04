package com.sunrisedental.dao.impl;

import com.sunrisedental.db.DBConnectionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the DB-level double-booking guard (schema.sql's
 * {@code trg_appointments_no_double_booking_insert}/{@code _update}
 * triggers) actually fires — deliberately using raw JDBC to insert
 * directly into {@code appointments}, bypassing
 * {@code AppointmentDAOImpl.hasClash()} entirely, the way a rogue script
 * or another application would. The Java-level check
 * ({@code AppointmentDAOImplTest}) is the primary, user-facing guard;
 * this is the defense-in-depth layer underneath it (schema.sql's comment
 * on why both exist).
 */
class DoubleBookingTriggerTest {

    private int dentistUserId;
    private int dentistId;
    private int patientId;
    private int treatmentId;
    private final LocalDate testDate = LocalDate.now().plusYears(2); // far enough out to never collide with real data

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO users (username, password, full_name, role) "
                    + "VALUES ('test.trigger.dentist', 'x', 'Test Trigger Dentist', 'DENTIST')");
            dentistUserId = lastInsertId(conn);
            dentistId = dentistUserId;
            stmt.executeUpdate("INSERT INTO dentists (dentist_id, daily_appointment_limit) VALUES ("
                    + dentistId + ", 20)");

            stmt.executeUpdate("INSERT INTO patients (name, address, contact_number) "
                    + "VALUES ('Test Trigger Patient', '1 Test Rd', '0710000000')");
            patientId = lastInsertId(conn);

            stmt.executeUpdate("INSERT INTO treatments (name, cost, duration_minutes) "
                    + "VALUES ('Test Trigger Treatment', 1000.00, 30)");
            treatmentId = lastInsertId(conn);
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM appointments WHERE dentist_id = " + dentistId);
            stmt.executeUpdate("DELETE FROM dentists WHERE dentist_id = " + dentistId);
            stmt.executeUpdate("DELETE FROM users WHERE user_id = " + dentistUserId);
            stmt.executeUpdate("DELETE FROM patients WHERE patient_id = " + patientId);
            stmt.executeUpdate("DELETE FROM treatments WHERE treatment_id = " + treatmentId);
        }
    }

    private int lastInsertId(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void insertAppointment(Connection conn, String time) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, dentist_id, treatment_id, "
                + "appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?, 'SCHEDULED')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, dentistId);
            stmt.setInt(3, treatmentId);
            stmt.setObject(4, testDate);
            stmt.setString(5, time);
            stmt.executeUpdate();
        }
    }

    @Test
    void trigger_blocksAnOverlappingRawInsert() throws SQLException {
        try (Connection conn = DBConnectionManager.getInstance().getConnection()) {
            insertAppointment(conn, "10:00:00"); // occupies 10:00-10:30

            SQLException thrown = assertThrows(SQLException.class, () -> insertAppointment(conn, "10:15:00"));
            assertTrue(thrown.getMessage().contains("Double-booking guard"));
        }
    }

    @Test
    void trigger_allowsABackToBackNonOverlappingRawInsert() throws SQLException {
        try (Connection conn = DBConnectionManager.getInstance().getConnection()) {
            insertAppointment(conn, "10:00:00"); // occupies 10:00-10:30

            assertDoesNotThrow(() -> insertAppointment(conn, "10:30:00"));
        }
    }
}
