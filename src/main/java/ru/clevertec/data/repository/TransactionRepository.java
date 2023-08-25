package ru.clevertec.data.repository;

import java.time.Instant;
import java.util.List;
import ru.clevertec.data.entity.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    List<Transaction> findAllTransactionsForUser(Instant startDate, Instant endDate, Long id);

}
