package com.sunrisedental.dao;

import com.sunrisedental.model.User;

import java.util.List;
import java.util.Optional;

/**
 * DAO design pattern (CLAUDE.md's suggested pattern list): isolates SQL
 * access to the {@code users} table behind a plain interface, so the
 * business tier (e.g. {@code UserService}, per sequence diagram 3.1)
 * depends only on this contract, never on JDBC directly.
 */
public interface UserDAO {

    /**
     * Looks up a staff user by username, for the Login use case. Returns
     * the concrete subtype (Receptionist/Administrator/Dentist) — the
     * implementation decides which based on a stored role/discriminator
     * column.
     */
    Optional<User> findByUsername(String username);

    Optional<User> findById(int userId);

    /** All staff accounts, for Manage Staff Accounts (Administrator use case, decision 5). */
    List<User> findAll();

    /**
     * Inserts a new staff account. Which concrete {@link User} subtype is
     * passed in decides both the {@code role} discriminator and whether a
     * {@code dentists} extension row is also created (decision 25) — the
     * caller doesn't need to know the table shape, just which subtype to
     * construct.
     */
    User save(User user);

    void deleteById(int userId);
}
