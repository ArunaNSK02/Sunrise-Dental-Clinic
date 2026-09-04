package com.sunrisedental.dao.impl;

import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.AvailabilityBlock;
import com.sunrisedental.model.Dentist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DentistDAOImpl implements DentistDAO {

    private static final String SELECT_DENTIST =
            "SELECT u.user_id, u.username, u.password, u.full_name, d.dentist_id, d.daily_appointment_limit "
            + "FROM dentists d JOIN users u ON u.user_id = d.dentist_id ";

    @Override
    public Optional<Dentist> findById(int dentistId) {
        String sql = SELECT_DENTIST + "WHERE d.dentist_id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dentistId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Dentist dentist = mapRow(rs);
                dentist.setUnavailablePeriods(findAvailabilityBlocks(dentistId));
                return Optional.of(dentist);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find dentist by id: " + dentistId, e);
        }
    }

    @Override
    public List<Dentist> findAll() {
        String sql = SELECT_DENTIST + "ORDER BY u.full_name";
        List<Dentist> dentists = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dentists.add(mapRow(rs));
            }
            return dentists;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list dentists", e);
        }
    }

    @Override
    public void updateDailyAppointmentLimit(int dentistId, int limit) {
        String sql = "UPDATE dentists SET daily_appointment_limit = ? WHERE dentist_id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, dentistId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update daily appointment limit for dentist: " + dentistId, e);
        }
    }

    @Override
    public void addAvailabilityBlock(int dentistId, AvailabilityBlock block) {
        String sql = "INSERT INTO availability_blocks (dentist_id, start_datetime, end_datetime, reason) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, dentistId);
            stmt.setTimestamp(2, Timestamp.valueOf(block.getStartDateTime()));
            stmt.setTimestamp(3, Timestamp.valueOf(block.getEndDateTime()));
            stmt.setString(4, block.getReason());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    block.setBlockId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add availability block for dentist: " + dentistId, e);
        }
    }

    @Override
    public List<AvailabilityBlock> findAvailabilityBlocks(int dentistId) {
        String sql = "SELECT block_id, start_datetime, end_datetime, reason "
                + "FROM availability_blocks WHERE dentist_id = ? ORDER BY start_datetime";
        List<AvailabilityBlock> blocks = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dentistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    blocks.add(new AvailabilityBlock(
                            rs.getInt("block_id"),
                            rs.getTimestamp("start_datetime").toLocalDateTime(),
                            rs.getTimestamp("end_datetime").toLocalDateTime(),
                            rs.getString("reason")));
                }
                return blocks;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find availability blocks for dentist: " + dentistId, e);
        }
    }

    @Override
    public int countAppointmentsOnDate(int dentistId, LocalDate date, int excludeAppointmentNumber) {
        String sql = "SELECT COUNT(*) FROM appointments "
                + "WHERE dentist_id = ? AND appointment_date = ? AND status <> 'CANCELLED' "
                + "AND appointment_number <> ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dentistId);
            stmt.setObject(2, date);
            stmt.setInt(3, excludeAppointmentNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to count appointments for dentist " + dentistId + " on " + date, e);
        }
    }

    private Dentist mapRow(ResultSet rs) throws SQLException {
        return new Dentist(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getInt("dentist_id"),
                rs.getInt("daily_appointment_limit"));
    }
}
