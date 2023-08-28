package ru.clevertec.data.repository;

import java.util.Optional;
import ru.clevertec.data.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
}
