package com.sunrisedental.model;

/**
 * Clinic administrator (class diagram, docs/DESIGN.md). Extends
 * {@link Receptionist} — every receptionist capability plus staff-account
 * management and clinic-wide reporting (decisions 4-5, 15).
 */
public class Administrator extends Receptionist {

    public Administrator() {
        super();
    }

    public Administrator(int userId, String username, String password, String fullName) {
        super(userId, username, password, fullName);
    }

    public void addStaffAccount(User user) {
        throw new UnsupportedOperationException("Delegates to UserService — not yet wired up.");
    }

    public void removeStaffAccount(int userId) {
        throw new UnsupportedOperationException("Delegates to UserService — not yet wired up.");
    }

    public void viewReports() {
        throw new UnsupportedOperationException("Delegates to a reporting service — not yet wired up.");
    }
}
