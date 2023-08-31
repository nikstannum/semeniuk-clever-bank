package ru.clevertec.data.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import ru.clevertec.data.entity.Transaction;

public interface TransactionRepository {

    /**
     * creates a new transaction
     *
     * @param transaction actual transaction
     * @param connection  database
     */
    void createTransaction(Transaction transaction, Connection connection);

    /**
     * returns all user transactions for the specified period
     *
     * @param startDate beginning of period
     * @param endDate   end of period
     * @param id        user's identifier
     * @return list of transactions
     */
    List<Transaction> findAllTransactionsForUser(Instant startDate, Instant endDate, Long id);

    /**
     * returns the user's income and expenses for the specified period
     *
     * @param startDate beginning of period
     * @param endDate   end of period
     * @param id        user's identifier
     * @return map with income and expense amount
     */
    Map<String, BigDecimal> findIncomeAndExpenseForUser(Instant startDate, Instant endDate, Long id);

}
