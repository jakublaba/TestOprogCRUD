package controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DatabaseGenerator {

    public static Connection connect(String url, String user, String password) throws SQLException {
        if (
            url == null || user == null || password == null
            || url.isBlank() || user.isBlank() || password.isBlank()
        ) {
            throw new IllegalArgumentException("Credentials must be non-null and not blank");
        }

        val connection = DriverManager.getConnection(url, user, password);
        log.info(String.format("%s established", connection));
        return connection;
    }

    // Searches for table_schema.sql resources, creates and populates table from it
    // Returns name of script names that were executed
    public static String generateFromScript(Connection connection, String table) throws SQLException {
        if (connection.isClosed()) {
            throw new SQLException("Connection must not be closed");
        }

        if (table == null || table.isBlank()) {
            throw new IllegalArgumentException("Table name must be non-null and not blank");
        }

        val script = String.format("%s_schema.sql", table);
        runScriptOnDb(connection, script);

        return script;
    }

    private static void runScriptOnDb(Connection connection, String script) {
        try (
            val fileReader = new FileReader(
                Objects.requireNonNull(DatabaseGenerator.class.getClassLoader().getResource(script)).getFile()
            );
            val bufferedReader = new BufferedReader(fileReader)
        ) {
            val scriptRunner = new ScriptRunner(connection);
            scriptRunner.runScript(bufferedReader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("%s - file not found", script), e);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unexpected exception while reading %s", script), e);
        }
    }

}
