package com.sunrisedental.service;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.dao.impl.UserDAOImpl;
import com.sunrisedental.model.User;

import java.util.Optional;

/**
 * Business tier for authentication, matching sequence diagram 3.1
 * (Login): {@code LoginServlet -> UserService -> UserDAO -> Database}.
 * {@link #verifyPassword} is the self-message from that diagram — the
 * credential check never leaves this tier.
 *
 * <p>Depends on the {@link UserDAO} interface, not {@link UserDAOImpl}
 * directly, so the DAO implementation can be swapped (e.g. for a test
 * double) without changing this class.</p>
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this(new UserDAOImpl());
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a staff member. Returns the matched {@link User} on
     * success, or empty on a bad username/password — the servlet decides
     * what to do with either outcome (create the session, or show an
     * error), per the {@code alt} fragment in sequence diagram 3.1.
     */
    public Optional<User> login(String username, String password) {
        Optional<User> user = userDAO.findByUsername(username);
        if (user.isPresent() && verifyPassword(user.get(), password)) {
            return user;
        }
        return Optional.empty();
    }

    /**
     * Self-message in sequence diagram 3.1 — plain equality for now.
     *
     * <p>Storing and comparing raw passwords is a placeholder, not a
     * design decision: production code must hash passwords (e.g. BCrypt)
     * before persisting or comparing them. Flagged as a known gap for the
     * report rather than left unmentioned.</p>
     */
    private boolean verifyPassword(User user, String suppliedPassword) {
        return user.getPassword() != null && user.getPassword().equals(suppliedPassword);
    }
}
