package controller;

public interface CRUD<T> {
    void create(T record);
    T read(long id);
    void update(long id, T newRecord);
    void delete(long id);
}