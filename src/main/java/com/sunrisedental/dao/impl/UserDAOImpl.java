package com.sunrisedental.dao.impl;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.Administrator;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Receptionist;
import com.sunrisedental.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * JDBC implementation of {@link UserDAO} — the first concrete DAO in the
 * project, demonstrating the pattern end-to-end against
 * {@link DBConnectionManager} (Singleton) and the {@code users}/{@code dentists}
 * tables in schema.sql.
 *
 * <p>The {@code role} column is a discriminator: this class maps a row to
 * the correct {@link User} subclass rather than the caller having to know
 * which one to construct.</p>
 */
public class UserDAOImpl implements UserDAO {

    private static final String SELECT_BASE =
            "SELECT u.user_id, u.username, u.password, u.full_name, u.role, "
            + "d.dentist_id, d.daily_appointment_limit "
            + "FROM users u LEFT JOIN dentists d ON d.dentist_id = u.user_id ";

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = SELECT_BASE + "WHERE u.username = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username: " + username, e);
        }
    }

    @Override
    public Optional<User> findById(int userId) {
        String sql = SELECT_BASE + "WHERE u.user_id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id: " + userId, e);
        }
    }

    @Override
    public User save(User user) {
        throw new UnsupportedOperationException(
                "Insert/update varies by concrete User subtype (role + dentist row) — not yet implemented.");
    }

    @Override
    public void deleteById(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user: " + userId, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String fullName = rs.getString("full_name");
        String role = rs.getString("role");

        User user = switch (role) {
            case "ADMINISTRATOR" -> new Administrator(userId, username, password, fullName);
            case "DENTIST" -> new Dentist(userId, username, password, fullName,
                    rs.getInt("dentist_id"), rs.getInt("daily_appointment_limit"));
            default -> new Receptionist(userId, username, password, fullName);
        };
        return user;
    }
}
