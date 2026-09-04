package com.sunrisedental.service;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.Receptionist;
import com.sunrisedental.model.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the login and Manage Staff Accounts methods, against a hand-written UserDAO stub. */
class UserServiceTest {

    private static class StubUserDAO implements UserDAO {
        final List<User> users = new ArrayList<>();
        int deletedUserId = -1;

        @Override
        public Optional<User> findByUsername(String username) {
            return users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
        }

        @Override
        public Optional<User> findById(int userId) {
            return users.stream().filter(u -> u.getUserId() == userId).findFirst();
        }

        @Override
        public List<User> findAll() {
            return users;
        }

        @Override
        public User save(User user) {
            user.setUserId(users.size() + 1);
            users.add(user);
            return user;
        }

        @Override
        public void deleteById(int userId) {
            deletedUserId = userId;
            users.removeIf(u -> u.getUserId() == userId);
        }
    }

    @Test
    void login_succeedsWithCorrectCredentials() {
        StubUserDAO dao = new StubUserDAO();
        dao.users.add(new Receptionist(1, "reception", "secret", "Nadeesha Fernando"));
        UserService service = new UserService(dao);

        Optional<User> result = service.login("reception", "secret");

        assertTrue(result.isPresent());
    }

    @Test
    void login_failsWithWrongPassword() {
        StubUserDAO dao = new StubUserDAO();
        dao.users.add(new Receptionist(1, "reception", "secret", "Nadeesha Fernando"));
        UserService service = new UserService(dao);

        assertFalse(service.login("reception", "wrong").isPresent());
    }

    @Test
    void addStaffAccount_delegatesToUserDAOSave() {
        StubUserDAO dao = new StubUserDAO();
        UserService service = new UserService(dao);

        User saved = service.addStaffAccount(new Receptionist(0, "new.staff", "x", "New Staff"));

        assertEquals(1, dao.users.size());
        assertTrue(saved.getUserId() > 0);
    }

    @Test
    void removeStaffAccount_delegatesToUserDAODelete() {
        StubUserDAO dao = new StubUserDAO();
        UserService service = new UserService(dao);

        service.removeStaffAccount(42);

        assertEquals(42, dao.deletedUserId);
    }

    @Test
    void listStaff_returnsEveryUser() {
        StubUserDAO dao = new StubUserDAO();
        dao.users.add(new Receptionist(1, "a", "x", "A"));
        dao.users.add(new Receptionist(2, "b", "x", "B"));
        UserService service = new UserService(dao);

        assertEquals(2, service.listStaff().size());
    }
}
