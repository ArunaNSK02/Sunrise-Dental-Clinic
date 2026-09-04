package com.sunrisedental.dao.impl;

import com.sunrisedental.dao.TreatmentDAO;
import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.Treatment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreatmentDAOImpl implements TreatmentDAO {

    @Override
    public Optional<Treatment> findById(int treatmentId) {
        String sql = "SELECT treatment_id, name, cost, duration_minutes FROM treatments WHERE treatment_id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, treatmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find treatment by id: " + treatmentId, e);
        }
    }

    @Override
    public List<Treatment> findAll() {
        String sql = "SELECT treatment_id, name, cost, duration_minutes FROM treatments ORDER BY name";
        List<Treatment> treatments = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                treatments.add(mapRow(rs));
            }
            return treatments;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list treatments", e);
        }
    }

    @Override
    public Treatment save(Treatment treatment) {
        String sql = "INSERT INTO treatments (name, cost, duration_minutes) VALUES (?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, treatment.getName());
            stmt.setDouble(2, treatment.getCost());
            stmt.setInt(3, treatment.getDurationMinutes());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    treatment.setTreatmentId(keys.getInt(1));
                }
            }
            return treatment;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save treatment: " + treatment.getName(), e);
        }
    }

    private Treatment mapRow(ResultSet rs) throws SQLException {
        return new Treatment(
                rs.getInt("treatment_id"),
                rs.getString("name"),
                rs.getDouble("cost"),
                rs.getInt("duration_minutes"));
    }
}
