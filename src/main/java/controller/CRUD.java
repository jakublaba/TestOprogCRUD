package controller;

import java.util.Optional;

public interface CRUD<T> {
    void create(T record);
    Optional<T> read(long id);
    void update(long id, T record);
    void delete(long id);
}