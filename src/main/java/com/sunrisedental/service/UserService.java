package com.sunrisedental.service;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.dao.impl.UserDAOImpl;
import com.sunrisedental.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
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
     * Self-message in sequence diagram 3.1. Uses {@link BCrypt#checkpw}
     * against the salted hash stored in {@code users.password} — plain-
     * text comparison was a placeholder flagged since the Task B scaffold
     * session (DESIGN.md's deviations log), now replaced for real.
     *
     * <p>Guards against a malformed/legacy (pre-hashing) stored value
     * rather than letting {@code BCrypt.checkpw} throw
     * {@code IllegalArgumentException} out of a login attempt — a bad
     * hash should mean "this login fails", not a 500 error.</p>
     */
    private boolean verifyPassword(User user, String suppliedPassword) {
        if (user.getPassword() == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(suppliedPassword, user.getPassword());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Manage Staff Accounts (Administrator use case, decision 5): create a
     * staff login. Hashes the supplied plain-text password before it ever
     * reaches the DAO/database — {@code UserDAOImpl.save()} just persists
     * whatever string it's given, so hashing belongs here, not there.
     */
    public User addStaffAccount(User user) {
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        return userDAO.save(user);
    }

    /** Manage Staff Accounts (Administrator use case, decision 5): remove a staff login. */
    public void removeStaffAccount(int userId) {
        userDAO.deleteById(userId);
    }

    public List<User> listStaff() {
        return userDAO.findAll();
    }
}
