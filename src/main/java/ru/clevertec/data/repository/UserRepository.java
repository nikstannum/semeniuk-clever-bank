package ru.clevertec.data.repository;

import java.util.Optional;
import ru.clevertec.data.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {
    /**
     * return user by its email
     *
     * @param email actual email
     * @return the object placed in an {@link java.util.Optional} container. Can be empty.
     */
    Optional<User> findUserByEmail(String email);
}
