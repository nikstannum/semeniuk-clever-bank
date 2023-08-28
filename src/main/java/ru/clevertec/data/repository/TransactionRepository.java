package ru.clevertec.data.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import ru.clevertec.data.entity.Transaction;

public interface TransactionRepository {

    void createTransaction(Transaction transaction, Connection connection);

    List<Transaction> findAllTransactionsForUser(Instant startDate, Instant endDate, Long id);

    Map<String, BigDecimal> findIncomeAndExpenseForUser(Instant startDate, Instant endDate, Long id);

}
