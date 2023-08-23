package ru.clevertec.data.repository.impl;

import java.util.List;
import java.util.Optional;
import ru.clevertec.data.entity.Transaction;
import ru.clevertec.data.repository.TransactionRepository;

public class TransactionRepositoryImpl implements TransactionRepository {
    @Override
    public Transaction create(Transaction entity) {
        return null;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Transaction> findAll(int page, int size) {
        return null;
    }

    @Override
    public Transaction update(Transaction entity) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }
}
