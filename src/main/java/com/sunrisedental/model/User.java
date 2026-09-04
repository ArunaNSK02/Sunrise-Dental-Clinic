package com.sunrisedental.model;

import java.util.Objects;

/**
 * Abstract base for every authenticated staff type (class diagram,
 * docs/DESIGN.md decisions 15-16). {@link Receptionist} and {@link Dentist}
 * extend this directly; {@link Administrator} extends {@code Receptionist}
 * — mirroring the actor generalization on the use case diagram with real
 * inheritance instead of just a diagram convention.
 *
 * <p>{@code login}/{@code logout} are defined once here rather than
 * repeated per subclass, since all three staff types authenticate
 * identically (decision 16). The actual credential check against the
 * database is business-tier responsibility ({@code UserService}), not this
 * class's — {@link #login} exists on the model to match the class diagram,
 * but in the running system {@code UserService.verifyPassword()} is what
 * the Login sequence diagram (docs/DESIGN.md, 3.1) actually calls.</p>
 */
public abstract class User {

    private int userId;
    private String username;
    private String password;
    private String fullName;

    protected User() {
    }

    protected User(int userId, String username, String password, String fullName) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    /**
     * Placeholder credential check kept on the model to match the class
     * diagram (decision 16). Real authentication happens in the business
     * tier against hashed passwords stored in the database — see
     * {@code com.sunrisedental.service.UserService}.
     */
    public boolean login(String username, String password) {
        return Objects.equals(this.username, username) && Objects.equals(this.password, password);
    }

    public void logout() {
        // Session invalidation is a presentation-tier concern (the
        // HttpSession lives on the servlet, not on this model object) —
        // see docs/DESIGN.md sequence diagram 3.1's note that session
        // handling stays at the presentation tier.
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
