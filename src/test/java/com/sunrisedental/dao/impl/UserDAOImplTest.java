package com.sunrisedental.dao.impl;

import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Receptionist;
import com.sunrisedental.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test against the real local MySQL database for
 * {@link UserDAOImpl#save} — the DAO method every earlier test class
 * deliberately worked around via raw JDBC (see {@code AppointmentDAOImplTest}'s
 * class javadoc) because it used to be a stub. Now that it's implemented,
 * this covers both the plain {@code users} insert (Receptionist) and the
 * extra {@code dentists} row a Dentist needs (decision 25).
 */
class UserDAOImplTest {

    private final UserDAOImpl userDAO = new UserDAOImpl();
    private int createdUserId = -1;

    @AfterEach
    void tearDown() throws SQLException {
        if (createdUserId < 0) {
            return;
        }
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM dentists WHERE dentist_id = " + createdUserId);
            stmt.executeUpdate("DELETE FROM users WHERE user_id = " + createdUserId);
        }
    }

    @Test
    void save_receptionist_insertsOnlyAUsersRow() {
        Receptionist receptionist = new Receptionist(0, "test.reception.dao", "x", "Test Reception DAO");

        userDAO.save(receptionist);
        createdUserId = receptionist.getUserId();

        assertTrue(createdUserId > 0);
        Optional<User> found = userDAO.findById(createdUserId);
        assertTrue(found.isPresent());
        assertEquals("test.reception.dao", found.get().getUsername());
        assertFalse(found.get() instanceof Dentist);
    }

    @Test
    void save_dentist_alsoInsertsADentistsRowWithTheDailyLimit() {
        Dentist dentist = new Dentist(0, "test.dentist.dao.save", "x", "Test Dentist DAO Save", 0, 15);

        userDAO.save(dentist);
        createdUserId = dentist.getUserId();

        Optional<User> found = userDAO.findById(createdUserId);
        assertTrue(found.isPresent());
        assertTrue(found.get() instanceof Dentist);
        assertEquals(15, ((Dentist) found.get()).getDailyAppointmentLimit());
    }

    @Test
    void findAll_includesASavedUser() {
        Receptionist receptionist = new Receptionist(0, "test.reception.findall", "x", "Test FindAll DAO");
        userDAO.save(receptionist);
        createdUserId = receptionist.getUserId();

        boolean present = userDAO.findAll().stream().anyMatch(u -> u.getUserId() == createdUserId);

        assertTrue(present);
    }

    @Test
    void deleteById_removesTheUser() {
        Receptionist receptionist = new Receptionist(0, "test.reception.delete", "x", "Test Delete DAO");
        userDAO.save(receptionist);
        int id = receptionist.getUserId();

        userDAO.deleteById(id);

        assertTrue(userDAO.findById(id).isEmpty());
        createdUserId = -1; // already deleted — nothing left for tearDown to clean up
    }
}
