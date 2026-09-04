package com.sunrisedental.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton design pattern (CLAUDE.md's suggested pattern list): a single
 * shared point of JDBC configuration for the whole data access tier,
 * rather than every DAO re-reading db.properties and re-registering the
 * driver independently.
 *
 * <p>This does NOT pool connections — {@link #getConnection()} opens a
 * fresh {@link Connection} per call, which callers are responsible for
 * closing (try-with-resources). What's "singleton" here is the loaded
 * configuration and driver registration, not the connection itself; a
 * real connection pool (HikariCP, or Tomcat's own JNDI DataSource) would
 * be a reasonable upgrade to note as a limitation in the report rather
 * than something this coursework build needs to implement from scratch.
 * </p>
 */
public final class DBConnectionManager {

    private static final DBConnectionManager INSTANCE = new DBConnectionManager();

    private final String url;
    private final String username;
    private final String password;

    private DBConnectionManager() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "db.properties not found on the classpath. Copy "
                        + "src/main/resources/db.properties.example to "
                        + "src/main/resources/db.properties and fill in your local MySQL credentials.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read db.properties", e);
        }

        this.url = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");

        try {
            Class.forName(props.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found on the classpath", e);
        }
    }

    public static DBConnectionManager getInstance() {
        return INSTANCE;
    }

    /** Opens a new JDBC connection. Callers must close it (try-with-resources). */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
