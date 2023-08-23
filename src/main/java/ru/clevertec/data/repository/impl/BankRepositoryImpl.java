package ru.clevertec.data.repository.impl;

import java.util.List;
import java.util.Optional;
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.repository.BankRepository;

public class BankRepositoryImpl implements BankRepository {
    @Override
    public Bank create(Bank entity) {
        return null;
    }

    @Override
    public Optional<Bank> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Bank> findAll(int page, int size) {
        return null;
    }

    @Override
    public Bank update(Bank entity) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }
}
