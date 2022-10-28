package controller;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.val;
import model.User;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;

public class CrudController implements CRUD<User> {
    private final DataSource dataSource;

    public CrudController(String url, String user, String password) {
        val config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        dataSource = new HikariDataSource(config);
    }

    @Override
    public void create(User record) throws SQLException {
        if(record.username().isBlank()) {
            throw new IllegalArgumentException("Username must not be blank.");
        }

        val sql = "INSERT INTO users VALUES(DEFAULT, ?)";
        try (
            val connection = dataSource.getConnection();
            val preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setString(1, record.username());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public Optional<User> read(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be greater than 0");
        }

        val sql = "SELECT * FROM users WHERE id = ?";
        try (
            val connection = dataSource.getConnection();
            val preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, id);
            val resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(new User(
                resultSet.getString("username")
            ));
        }
    }

    @Override
    public void update(int id, User newRecord) throws SQLException {
        if (newRecord == null){
            throw new IllegalArgumentException("User must not be null");
        }
        if (newRecord.username().isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }

        val sql =
            """
            UPDATE users
            SET username = ?
            WHERE id = ?
            """;
        try (
            val connection = dataSource.getConnection();
            val preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setString(1, newRecord.username());
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }

        val sql = "DELETE FROM users WHERE id = ?";
        try (
            val connection = dataSource.getConnection();
            val preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }

}
