package controller;

import java.sql.SQLException;
import java.util.Optional;

public interface CRUD<T> {
    void create(T record) throws SQLException;
    Optional<T> read(long id) throws SQLException;
    void update(long id, T record) throws SQLException;
    void delete(long id) throws SQLException;
}