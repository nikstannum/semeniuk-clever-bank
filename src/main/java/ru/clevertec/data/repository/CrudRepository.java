package ru.clevertec.data.repository;

import java.util.List;
import java.util.Optional;

/**
 * Standard repository for create, read, update, and delete operations.
 *
 * @param <T> the type of object
 * @param <K> object identifier
 */
public interface CrudRepository<T, K> {
    /**
     * method to create an object
     *
     * @param entity for creation
     * @return the created object
     */
    T create(T entity);

    /**
     * method to get an object by its id
     *
     * @param id object identifier
     * @return the object placed in an {@link java.util.Optional} container. Can be empty.
     */
    Optional<T> findById(K id);

    /**
     * method for getting a paginated list of objects
     *
     * @param limit  the list size
     * @param offset number of objects behind the list
     * @return the list of objects
     */
    List<T> findAll(int limit, long offset);

    /**
     * method for updating of an object
     *
     * @param entity for updating
     * @return the updated object
     */
    T update(T entity);

    /**
     * method to delete an object by its identifier
     */
    void deleteById(K id);

}
