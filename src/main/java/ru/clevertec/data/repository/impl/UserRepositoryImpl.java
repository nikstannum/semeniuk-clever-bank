package ru.clevertec.data.repository.impl;

import java.util.List;
import java.util.Optional;
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.UserRepository;

public class UserRepositoryImpl implements UserRepository {
    @Override
    public User create(User entity) {
        return null;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<User> findAll(int page, int size) {
        return null;
    }

    @Override
    public User update(User entity) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }
}
