package com.sunrisedental.dao.impl;

import com.sunrisedental.dao.ClinicSettingsDAO;
import com.sunrisedental.db.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClinicSettingsDAOImpl implements ClinicSettingsDAO {

    @Override
    public double getConsultationFee() {
        String sql = "SELECT consultation_fee FROM clinic_settings WHERE id = 1";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
                throw new IllegalStateException(
                        "clinic_settings has no row — schema.sql's default INSERT is missing or was deleted.");
            }
            return rs.getDouble("consultation_fee");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read consultation fee", e);
        }
    }
}
