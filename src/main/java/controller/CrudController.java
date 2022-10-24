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
    public void create(User record) {
        val sql = "INSERT INTO users VALUES(DEFAULT, ?)";
        try (
            val connection = dataSource.getConnection();
            val preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setString(1, record.username());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> read(long id) {
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(long id, User newRecord) {
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(long id) {
        val sql = "DELETE FROM users WHERE id = ?";
        try (
            val connection = dataSource.getConnection();
            val preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}