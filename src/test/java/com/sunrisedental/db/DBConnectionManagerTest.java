package com.sunrisedental.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confirms DBConnectionManager (Singleton) actually opens a working
 * connection against the local MySQL instance described in
 * src/main/resources/db.properties. This is the one JUnit test in the
 * project that requires a real, reachable database — everything else
 * (e.g. BillTest) is deliberately DB-free.
 */
class DBConnectionManagerTest {

    @Test
    void getInstance_returnsTheSameSingletonEveryTime() {
        DBConnectionManager first = DBConnectionManager.getInstance();
        DBConnectionManager second = DBConnectionManager.getInstance();

        assertTrue(first == second);
    }

    @Test
    void getConnection_opensAWorkingConnectionToMySQL() throws SQLException {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            assertFalse(connection.isClosed());
            assertTrue(connection.isValid(2));
        }
    }
}
