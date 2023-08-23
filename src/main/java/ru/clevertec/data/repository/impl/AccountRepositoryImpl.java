package ru.clevertec.data.repository.impl;

import java.util.List;
import java.util.Optional;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.repository.AccountRepository;

public class AccountRepositoryImpl implements AccountRepository {
    @Override
    public Account create(Account entity) {
        return null;
    }

    @Override
    public Optional<Account> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Account> findAll(int page, int size) {
        return null;
    }

    @Override
    public Account update(Account entity) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }
}
