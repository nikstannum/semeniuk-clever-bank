package ru.clevertec.data.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, K> {

    T create(T entity);

    Optional<T> findById(K id);

    List<T> findAll(int page, int size);

    T update(T entity);

    boolean delete(K id);

}
