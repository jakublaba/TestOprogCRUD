package controller;

import java.sql.SQLException;
import java.util.Optional;

public interface CRUD<T> {
    void create(T record);
    Optional<T> read(long id) throws SQLException;
    void update(long id, T record);
    void delete(long id);
}