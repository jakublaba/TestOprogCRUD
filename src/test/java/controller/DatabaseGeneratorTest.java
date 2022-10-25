package controller;

import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseGeneratorTest {

    private static final String VALID_URL = "jdbc:postgresql://localhost:5431/";
    private static final String VALID_USER = "admin";
    private static final String VALID_PASSWORD = "admin";
    private static Connection VALID_CONNECTION;

    @BeforeAll
    static void connectionSetup() {
        try {
            VALID_CONNECTION = DriverManager.getConnection(VALID_URL, VALID_USER, VALID_PASSWORD);
        } catch (SQLException ignored) {}
    }

    @AfterAll
    static void connectionTeardown() {
        try {
            VALID_CONNECTION.close();
        } catch (SQLException ignored) {}
    }

    @CartesianTest(name = "{index} -> url: {0}, user: {1}, password: {2}")
    @DisplayName("DatabaseGenerator#connect - empty credentials")
    void connect_shouldThrow_whenAnyCredentialBlank(
        @Values(strings = {"", "\t", "\r", "\n", "\f", " ", VALID_URL}) String url,
        @Values(strings = {"", "\t", "\r", "\n", "\f", " ", VALID_USER}) String user,
        @Values(strings = {"", "\t", "\r", "\n", "\f", " ", VALID_PASSWORD}) String password
    ) {
        if (url.equals(VALID_URL) && user.equals(VALID_USER) && password.equals(VALID_PASSWORD)) {
            // Ignore combination that is not being tested in this case
            return;
        }

        Exception e = assertThrows(IllegalArgumentException.class, () -> DatabaseGenerator.connect(url, user, password));
        assertEquals("Credentials must be non-null and not blank", e.getMessage());
    }

    @CartesianTest(name = "{index} -> url: {0}, user: {1}, password: {2}")
    @DisplayName("DatabaseGenerator#connect - invalid credentials")
    void connect_shouldThrow_whenAnyCredentialInvalid(
        @Values(strings = {"invalidUrl", VALID_URL}) String url,
        @Values(strings = {"invalidUser", VALID_USER}) String user,
        @Values(strings = {"invalidPassword", VALID_PASSWORD}) String password
    ) {
        if (url.equals(VALID_URL) && user.equals(VALID_USER) && password.equals(VALID_PASSWORD)) {
            // Ignore combination that is not being tested in this case
            return;
        }

        assertThrows(SQLException.class, () -> DatabaseGenerator.connect(url, user, password));
    }

    @Test
    @DisplayName("DatabaseGenerator#connect - valid credentials")
    void connect_shouldReturnOpenedConnection_whenCredentialsValid() {
        assertDoesNotThrow(() -> DatabaseGenerator.connect(VALID_URL, VALID_USER, VALID_PASSWORD));
        try (val connection = DatabaseGenerator.connect(VALID_URL, VALID_USER, VALID_PASSWORD)) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        } catch (SQLException ignored) {}
    }

    @ParameterizedTest
    @DisplayName("DatabaseGenerator#generateFromScript - closed connection")
    @ValueSource(strings = {"users", "non-existent-table"})
    void generateFromScript_shouldThrow_whenConnectionClosed(String table) {
        Connection connection = mock(Connection.class);

        try {
            when(connection.isClosed()).thenReturn(true);
        } catch (SQLException ignored) {}

        Exception e = assertThrows(SQLException.class, () -> DatabaseGenerator.generateFromScript(connection, table));
        assertEquals("Connection must not be closed", e.getMessage());
    }

    @ParameterizedTest
    @DisplayName("DatabaseGenerator#generateFromScript - invalid table name")
    @ValueSource(strings = {"non-existent-table"})
    void generateFromScript_shouldThrow_whenTableNameInvalid(String table) {
        assertThrows(NullPointerException.class, () -> DatabaseGenerator.generateFromScript(VALID_CONNECTION, table));
    }

    @ParameterizedTest
    @DisplayName("DatabaseGenerator#generateFromScript - null table name")
    @NullSource
    void generateFromScript_shouldThrow_whenTableNameNull(String table) {
        assertThrows(NullPointerException.class, () -> DatabaseGenerator.generateFromScript(VALID_CONNECTION, table));
    }

    @ParameterizedTest(name = "{index} -> table: {0}")
    @DisplayName("DatabaseGenerator#generateFromScript - blank table name")
    @ValueSource(strings = {"", "\t", "\r", "\n", "\f", " "})
    void generateFromScript_shouldThrow_whenTableNameBlank(String table) {
        Exception e = assertThrows(
            IllegalArgumentException.class, () -> DatabaseGenerator.generateFromScript(VALID_CONNECTION, table)
        );
        assertEquals("Table name must not be blank", e.getMessage());
    }

    @ParameterizedTest
    @DisplayName("DatabaseGenerator#generateFromScript - valid table name")
    @ValueSource(strings = {"users"})
    void generateFromScript_shouldRunCorrectScripts_whenTableNameValid(String table) {
        val expectedScript = "users_schema.sql";
        String scriptsRan = null;
        try {
            scriptsRan = DatabaseGenerator.generateFromScript(VALID_CONNECTION, table);
        } catch (SQLException ignored) {}

        assertEquals(expectedScript, scriptsRan);
    }

}