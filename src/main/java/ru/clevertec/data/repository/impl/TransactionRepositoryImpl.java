package ru.clevertec.data.repository.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Transaction;
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.TransactionRepository;

@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {
    private static final String FIND_ALL = """
            SELECT t.id, t.account_id, t.destination_account_id, t.account_amount, t.destination_account_amount, t."time"
            FROM transactions t
            WHERE t.deleted = false
            ORDER BY t.id
            LIMIT ?
            OFFSET ?
            """;
    private static final String DELETE_BY_ID = """
            UPDATE transactions
            SET deleted = true
            WHERE id = ?
            """;
    private static final String UPDATE_TRANSACTION = """
            UPDATE transactions
            SET account_id = ?, destination_account_id = ?, account_amount = ?, destination_account_amount = ?
            WHERE id = ?
            """;
    private static final String CREATE_TRANSACTION = """
            INSERT INTO transactions (account_id, destination_account_id, account_amount, destination_account_amount)
            VALUES
            (?, ?, ?, ?)
            """;
    private static final String FIND_ALL_TRANSACTION_FOR_USER = """
            SELECT t.id, t.account_id, t.destination_account_id, t.account_amount, t.destination_account_amount, t."time",
            u1.last_name AS user_from, u2.last_name AS user_to
            FROM transactions t
            LEFT JOIN accounts a1 ON t.account_id = a1.id
            LEFT JOIN users u1 ON a1.user_id = u1.id
            LEFT JOIN accounts a2 ON t.destination_account_id = a2.id
            LEFT JOIN users u2 ON a2.user_id = u2.id
            WHERE t.deleted = false
            AND t."time" >= ?
            AND t."time" <= ?
            AND t.account_id = ? OR t.destination_account_id = ?
            ORDER BY t."time"
            """;
    private static final String FIND_INCOME_EXPENSE = """
            SELECT
                (SELECT COALESCE(SUM(account_amount), 0)
                FROM transactions t
                WHERE t.account_id = ?
                AND t.deleted = FALSE
                AND t."time" >= ?
            	AND t."time" <= ?) AS income,
                (SELECT COALESCE(SUM(destination_account_amount), 0)
                FROM transactions t
                WHERE t.destination_account_id = ?
                AND t.deleted = FALSE
                AND t."time" >= ?
            	AND t."time" <= ?) AS expense
            """;
    private final DataSource dataSource;

    @Override
    public Map<String, BigDecimal> findIncomeAndExpenseForUser(Instant startDate, Instant endDate, Long id) {
        Map<String, BigDecimal> map = new HashMap<>();
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_INCOME_EXPENSE)) {
            statement.setLong(1, id);
            statement.setTimestamp(2, Timestamp.from(startDate));
            statement.setTimestamp(3, Timestamp.from(endDate));
            statement.setLong(4, id);
            statement.setTimestamp(5, Timestamp.from(startDate));
            statement.setTimestamp(6, Timestamp.from(endDate));
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            map.put("income", resultSet.getBigDecimal("income"));
            map.put("expense", resultSet.getBigDecimal("expense"));
            return map;
        } catch (SQLException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createTransaction(Transaction transaction, Connection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement(CREATE_TRANSACTION, Statement.RETURN_GENERATED_KEYS);
            Account accountFrom = transaction.getAccountFrom();
            if (accountFrom == null) {
                statement.setObject(1, null);
                statement.setBigDecimal(3, null);
            } else {
                statement.setLong(1, accountFrom.getId());
                statement.setBigDecimal(3, transaction.getAccountFromAmount());
            }
            Account accountTo = transaction.getAccountTo();
            if (accountTo == null) {
                statement.setObject(2, null);
                statement.setBigDecimal(4, null);
            } else {
                statement.setLong(2, accountTo.getId());
                statement.setBigDecimal(4, transaction.getAccountToAmount());
            }
            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            while (keys.next()) {
                Long id = keys.getLong("id");
                transaction.setId(id);
                Instant time = keys.getTimestamp("time").toInstant();
                transaction.setTransactionTime(time);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Transaction> findAllTransactionsForUser(Instant startDate, Instant endDate, Long id) {
        List<Transaction> list = new ArrayList<>();
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_TRANSACTION_FOR_USER)) {
            statement.setTimestamp(1, Timestamp.from(startDate));
            statement.setTimestamp(2, Timestamp.from(endDate));
            statement.setLong(3, id);
            statement.setLong(4, id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(processTransactionsForUser(resultSet));
            }
            return list;
        } catch (SQLException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
    }

    private Transaction processTransactionsForUser(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setAccountFromAmount(rs.getBigDecimal("account_amount"));
        transaction.setAccountToAmount(rs.getBigDecimal("destination_account_amount"));
        transaction.setTransactionTime(rs.getTimestamp("time").toInstant());
        User userFrom = new User();
        userFrom.setLastName(rs.getString("user_from"));
        Account accountFrom = new Account();
        accountFrom.setId(rs.getLong("account_id"));
        accountFrom.setUser(userFrom);
        transaction.setAccountFrom(accountFrom);
        User userTo = new User();
        userTo.setLastName(rs.getString("user_to"));
        Account accountTo = new Account();
        accountTo.setId(rs.getLong("destination_account_id"));
        accountTo.setUser(userTo);
        transaction.setAccountTo(accountTo);
        return transaction;
    }


    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.empty();
    }

    //    @Override
//    public Optional<Transaction> findById(Long id) {
//        try (Connection connection = dataSource.getFreeConnections();
//             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
//            statement.setLong(1, id);
//            ResultSet resultSet = statement.executeQuery();
//            if (resultSet.next()) {
//                return Optional.of(process(resultSet));
//            }
//        } catch (SQLException e) {
//            // FIXME add logging
//        }
//        return Optional.empty();
//    }
//
//    private Transaction process(ResultSet rs) throws SQLException {
//        Transaction transaction = new Transaction();
//        transaction.setId(rs.getLong("id"));
//        transaction.setAccountFromId(rs.getLong("account_id"));
//        transaction.setAccountToId(rs.getLong("destination_account_id"));
//        transaction.setAccountFromAmount(rs.getBigDecimal("account_amount"));
//        transaction.setAccountToAmount(rs.getBigDecimal("destination_account_amount"));
//        transaction.setTransactionTime(rs.getTimestamp("time").toInstant());
//        return transaction;
//    }
}
