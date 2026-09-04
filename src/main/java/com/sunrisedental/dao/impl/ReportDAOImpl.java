package com.sunrisedental.dao.impl;

import com.sunrisedental.dao.DentistLoad;
import com.sunrisedental.dao.ReportDAO;
import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.AppointmentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ReportDAOImpl implements ReportDAO {

    @Override
    public Map<AppointmentStatus, Integer> countAppointmentsByStatus() {
        Map<AppointmentStatus, Integer> counts = new EnumMap<>(AppointmentStatus.class);
        for (AppointmentStatus status : AppointmentStatus.values()) {
            counts.put(status, 0);
        }
        String sql = "SELECT status, COUNT(*) AS c FROM appointments GROUP BY status";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                counts.put(AppointmentStatus.valueOf(rs.getString("status")), rs.getInt("c"));
            }
            return counts;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count appointments by status", e);
        }
    }

    @Override
    public double totalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bills";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to total revenue", e);
        }
    }

    @Override
    public List<DentistLoad> appointmentLoadByDentist(LocalDate date) {
        String sql = "SELECT u.full_name, COUNT(*) AS c FROM appointments a "
                + "JOIN dentists d ON d.dentist_id = a.dentist_id "
                + "JOIN users u ON u.user_id = d.dentist_id "
                + "WHERE a.appointment_date = ? AND a.status <> 'CANCELLED' "
                + "GROUP BY u.full_name ORDER BY u.full_name";
        List<DentistLoad> loads = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loads.add(new DentistLoad(rs.getString("full_name"), rs.getInt("c")));
                }
            }
            return loads;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to compute appointment load for " + date, e);
        }
    }
}
