package controller;

import java.sql.SQLException;
import java.util.Optional;

public interface CRUD<T> {
    void create(T record) throws SQLException;
    Optional<T> read(int id) throws SQLException;
    void update(int id, T record) throws SQLException;
    void delete(int id) throws SQLException;
}