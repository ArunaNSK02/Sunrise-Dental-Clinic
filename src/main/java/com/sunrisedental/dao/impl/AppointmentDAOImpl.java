package com.sunrisedental.dao.impl;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.model.ChangeReason;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link AppointmentDAO}. Joins in enough of
 * patients/dentists/treatments/users to build a fully-populated
 * {@link Appointment} object graph per row, rather than making the caller
 * re-fetch each association separately.
 */
public class AppointmentDAOImpl implements AppointmentDAO {

    private static final String SELECT_BASE =
            "SELECT a.appointment_number, a.appointment_date, a.appointment_time, a.status, "
            + "a.change_reason, a.delay_minutes, "
            + "p.patient_id, p.name AS patient_name, p.address AS patient_address, p.contact_number, "
            + "u.user_id AS dentist_user_id, u.username, u.password, u.full_name, "
            + "d.dentist_id, d.daily_appointment_limit, "
            + "t.treatment_id, t.name AS treatment_name, t.cost, t.duration_minutes "
            + "FROM appointments a "
            + "JOIN patients p ON p.patient_id = a.patient_id "
            + "JOIN dentists d ON d.dentist_id = a.dentist_id "
            + "JOIN users u ON u.user_id = d.dentist_id "
            + "JOIN treatments t ON t.treatment_id = a.treatment_id ";

    @Override
    public Optional<Appointment> findByAppointmentNumber(int appointmentNumber) {
        String sql = SELECT_BASE + "WHERE a.appointment_number = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find appointment: " + appointmentNumber, e);
        }
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) {
        String sql = SELECT_BASE + "WHERE p.patient_id = ? ORDER BY a.appointment_date, a.appointment_time";
        return queryList(sql, patientId);
    }

    @Override
    public List<Appointment> findByDentistAndDate(int dentistId, LocalDate date) {
        String sql = SELECT_BASE
                + "WHERE d.dentist_id = ? AND a.appointment_date = ? ORDER BY a.appointment_time";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dentistId);
            stmt.setObject(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Appointment> appointments = new ArrayList<>();
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
                return appointments;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find appointments for dentist " + dentistId + " on " + date, e);
        }
    }

    /**
     * Test 1 of the three-test availability check (decision 24): does the
     * requested [time, time + durationMinutes) window overlap any
     * non-cancelled appointment this dentist already has that day? Two
     * intervals [s1,e1) and [s2,e2) overlap exactly when s1 &lt; e2 AND
     * s2 &lt; e1 — computed in SQL using each existing appointment's own
     * treatment duration, not a fixed slot size.
     */
    @Override
    public boolean hasClash(int dentistId, LocalDate date, LocalTime time, int durationMinutes,
                             int excludeAppointmentNumber) {
        String sql =
                "SELECT COUNT(*) FROM appointments a JOIN treatments t ON t.treatment_id = a.treatment_id "
                + "WHERE a.dentist_id = ? AND a.appointment_date = ? AND a.status <> 'CANCELLED' "
                + "AND a.appointment_number <> ? "
                + "AND a.appointment_time < ADDTIME(?, SEC_TO_TIME(? * 60)) "
                + "AND ? < ADDTIME(a.appointment_time, SEC_TO_TIME(t.duration_minutes * 60))";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dentistId);
            stmt.setObject(2, date);
            stmt.setInt(3, excludeAppointmentNumber);
            stmt.setTime(4, Time.valueOf(time));
            stmt.setInt(5, durationMinutes);
            stmt.setTime(6, Time.valueOf(time));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to check appointment clash for dentist " + dentistId + " on " + date + " at " + time, e);
        }
    }

    @Override
    public Appointment save(Appointment appointment) {
        String sql = "INSERT INTO appointments "
                + "(patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, appointment.getPatient().getPatientId());
            stmt.setInt(2, appointment.getDentist().getDentistId());
            stmt.setInt(3, appointment.getTreatment().getTreatmentId());
            stmt.setObject(4, appointment.getDate());
            stmt.setTime(5, Time.valueOf(appointment.getTime()));
            stmt.setString(6, appointment.getStatus().name());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    appointment.setAppointmentNumber(keys.getInt(1));
                }
            }
            return appointment;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save appointment", e);
        }
    }

    @Override
    public void update(Appointment appointment) {
        String sql = "UPDATE appointments SET appointment_date = ?, appointment_time = ?, status = ?, "
                + "change_reason = ?, delay_minutes = ? WHERE appointment_number = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, appointment.getDate());
            stmt.setTime(2, Time.valueOf(appointment.getTime()));
            stmt.setString(3, appointment.getStatus().name());
            stmt.setString(4, appointment.getChangeReason() == null ? null : appointment.getChangeReason().name());
            stmt.setInt(5, appointment.getDelayMinutes());
            stmt.setInt(6, appointment.getAppointmentNumber());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update appointment: " + appointment.getAppointmentNumber(), e);
        }
    }

    private List<Appointment> queryList(String sql, int id) {
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Appointment> appointments = new ArrayList<>();
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
                return appointments;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to run appointment query", e);
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Patient patient = new Patient(
                rs.getInt("patient_id"), rs.getString("patient_name"),
                rs.getString("patient_address"), rs.getString("contact_number"));

        Dentist dentist = new Dentist(
                rs.getInt("dentist_user_id"), rs.getString("username"), rs.getString("password"),
                rs.getString("full_name"), rs.getInt("dentist_id"), rs.getInt("daily_appointment_limit"));

        Treatment treatment = new Treatment(
                rs.getInt("treatment_id"), rs.getString("treatment_name"),
                rs.getDouble("cost"), rs.getInt("duration_minutes"));

        Appointment appointment = new Appointment(
                rs.getInt("appointment_number"), patient, dentist, treatment,
                rs.getObject("appointment_date", LocalDate.class),
                rs.getTime("appointment_time").toLocalTime());

        appointment.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        String changeReason = rs.getString("change_reason");
        if (changeReason != null) {
            appointment.setChangeReason(ChangeReason.valueOf(changeReason));
        }
        appointment.setDelayMinutes(rs.getInt("delay_minutes"));
        return appointment;
    }
}
