package controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseGeneratorTest {

    private static final String VALID_URL = "jdbc:postgresql://localhost:5431/";
    private static final String VALID_USER = "admin";
    private static final String VALID_PASSWORD = "admin";

    @CartesianTest(name = "{index} -> url: {0}, user: {1}, password: {2}")
    @DisplayName("Empty credentials test")
    void connect_shouldThrow_whenAnyCredentialEmptyOrBlank(
        @Values(strings = {"", "\t", "\r", "\n", "\f", "\b", " ", VALID_URL}) String url,
        @Values(strings = {"", "\t", "\r", "\n", "\f", "\b", " ", VALID_USER}) String user,
        @Values(strings = {"", "\t", "\r", "\n", "\f", "\b", " ", VALID_PASSWORD}) String password
    ) {
        if (url.equals(VALID_URL) && user.equals(VALID_USER) && password.equals(VALID_PASSWORD)) {
            // Ignore combination that is not being tested in this case
            return;
        }

        Exception e = assertThrows(IllegalArgumentException.class, () -> DatabaseGenerator.connect(url, user, password));
        assertEquals("Credentials must be non-null and not blank", e.getMessage());
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {"users", "non-existent-table"})
    void generateFromScript_shouldThrow_whenConnectionClosed(String tableName) {
        Connection connection = mock(Connection.class);

        when(connection.isClosed()).thenReturn(true);

        Exception e = assertThrows(SQLException.class, () -> DatabaseGenerator.generateFromScript(connection, tableName));
        assertEquals("Connection must not be closed", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"users"})
    void generateFromScript_shouldRunCorrectScripts_whenTableNameValid(String tableName) {

    }

}