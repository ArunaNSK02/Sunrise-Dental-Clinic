package com.sunrisedental.dao.impl;

import com.sunrisedental.dao.PatientDAO;
import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAOImpl implements PatientDAO {

    @Override
    public Optional<Patient> findById(int patientId) {
        String sql = "SELECT patient_id, name, address, contact_number FROM patients WHERE patient_id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find patient by id: " + patientId, e);
        }
    }

    @Override
    public Optional<Patient> findByContactNumber(String contactNumber) {
        String sql = "SELECT patient_id, name, address, contact_number FROM patients WHERE contact_number = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, contactNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find patient by contact number: " + contactNumber, e);
        }
    }

    @Override
    public List<Patient> findAll() {
        String sql = "SELECT patient_id, name, address, contact_number FROM patients ORDER BY name";
        List<Patient> patients = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                patients.add(mapRow(rs));
            }
            return patients;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list patients", e);
        }
    }

    @Override
    public Patient save(Patient patient) {
        String sql = "INSERT INTO patients (name, address, contact_number) VALUES (?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, patient.getName());
            stmt.setString(2, patient.getAddress());
            stmt.setString(3, patient.getContactNumber());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    patient.setPatientId(keys.getInt(1));
                }
            }
            return patient;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save patient: " + patient.getName(), e);
        }
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("patient_id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("contact_number"));
    }
}
