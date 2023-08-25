package ru.clevertec.data.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, K> {

    T create(T entity);

    Optional<T> findById(K id);

    List<T> findAll(int limit, long offset);

    T update(T entity);

    boolean deleteById(K id);

}
