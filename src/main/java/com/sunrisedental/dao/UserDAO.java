package com.sunrisedental.dao;

import com.sunrisedental.model.User;

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

    User save(User user);

    void deleteById(int userId);
}
