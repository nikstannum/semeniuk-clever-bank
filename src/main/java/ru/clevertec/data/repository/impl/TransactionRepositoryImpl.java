package ru.clevertec.data.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.data.entity.Transaction;
import ru.clevertec.data.repository.TransactionRepository;
import ru.clevertec.service.exception.NotFoundException;

@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {
    private final DataSource dataSource;

    private static final String CHANGE_AMOUNT_SIZE = """
            UPDATE accounts
            SET amount = ?
            WHERE id = ?
            """;

    private static final String CREATE_TRANSACTION = """
            INSERT INTO transactions (account_id, destination_account_id, account_amount, destination_account_amount)
            VALUES
            (?, ?, ?, ?)
            """;

    private static final String FIND_BY_ID = """
            SELECT t.id, t.account_id, t.destination_account_id, t.account_amount, t.destination_account_amount, t."time"
            FROM transactions t
            WHERE t.id = ? AND deleted = false;
            """;

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

    private static final String FIND_ALL_TRANSACTION_FOR_USER = """
            SELECT t.id, t.account_id, t.destination_account_id, t.account_amount, t.destination_account_amount, t."time"
            FROM transactions t
            WHERE t.deleted = false
            AND t."time" >= ?
            AND t."time" <= ?
            AND t.account_id = ?
            AND t.destination_account_id = ?
            ORDER BY t."time"
            """;

    @Override
    public Transaction create(Transaction transaction) {
        Connection connection = dataSource.getFreeConnections();
        try {
            connection.setAutoCommit(false);
            PreparedStatement initiatorStatement = connection.prepareStatement(CHANGE_AMOUNT_SIZE);
            initiatorStatement.setBigDecimal(1, transaction.getAccountAmount());
            initiatorStatement.setLong(2, transaction.getAccountId());
            initiatorStatement.executeUpdate();
            PreparedStatement destinationStatement = connection.prepareStatement(CHANGE_AMOUNT_SIZE);
            destinationStatement.setBigDecimal(1, transaction.getDestinationAccountAmount());
            destinationStatement.setLong(2, transaction.getDestinationAccountId());
            destinationStatement.executeUpdate();
            PreparedStatement transactionStatement = connection.prepareStatement(CREATE_TRANSACTION, Statement.RETURN_GENERATED_KEYS);
            transactionStatement.setLong(1, transaction.getAccountId());
            transactionStatement.setLong(2, transaction.getDestinationAccountId());
            transactionStatement.setBigDecimal(3, transaction.getAccountAmount());
            transactionStatement.setBigDecimal(4, transaction.getDestinationAccountAmount());
            transactionStatement.executeUpdate();
            ResultSet keys = transactionStatement.getGeneratedKeys();
            Long id = keys.getLong("id");
            connection.commit();
            return findById(id).orElseThrow(() -> new NotFoundException("couldn't find transaction after its creation"));
        } catch (SQLException e) {
            // FIXME add logging
            rollback(connection);
        } finally {
            restore(connection);
        }
        throw new RuntimeException("Couldn't make transaction. Try again");
    }

    @Override
    public List<Transaction> findAllTransactionsForUser(Instant startDate, Instant endDate, Long id) {
        List<Transaction> list = new ArrayList<>();
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_TRANSACTION_FOR_USER)) {
            statement.setObject(1, startDate);
            statement.setObject(2, endDate);
            statement.setLong(3, id);
            statement.setLong(4, id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(process(resultSet));
            }
            return list;
        } catch (SQLException e) {
            // FIXME add logging
        }
        return list;
    }

    @Override
    public Transaction update(Transaction transaction) {
        Connection connection = dataSource.getFreeConnections();
        try {
            connection.setAutoCommit(false);
            PreparedStatement initiatorStatement = connection.prepareStatement(CHANGE_AMOUNT_SIZE);
            initiatorStatement.setBigDecimal(1, transaction.getAccountAmount());
            initiatorStatement.setLong(2, transaction.getAccountId());
            initiatorStatement.executeUpdate();
            PreparedStatement destinationStatement = connection.prepareStatement(CHANGE_AMOUNT_SIZE);
            destinationStatement.setBigDecimal(1, transaction.getDestinationAccountAmount());
            destinationStatement.setLong(2, transaction.getDestinationAccountId());
            destinationStatement.executeUpdate();
            PreparedStatement transactionStatement = connection.prepareStatement(UPDATE_TRANSACTION);
            transactionStatement.setLong(1, transaction.getAccountId());
            transactionStatement.setLong(2, transaction.getDestinationAccountId());
            transactionStatement.setBigDecimal(3, transaction.getAccountAmount());
            transactionStatement.setBigDecimal(4, transaction.getDestinationAccountAmount());
            transactionStatement.setLong(5, transaction.getId());
            int rowUpd = transactionStatement.executeUpdate();
            connection.commit();
            if (rowUpd == 1) {
                return findById(transaction.getId()).orElseThrow(() -> new NotFoundException("couldn't find transaction after its creation"));
            }
            // FIXME add logging
            throw new SQLException(rowUpd + " row(s) was(were) updated");
        } catch (SQLException e) {
            // FIXME add logging
            rollback(connection);
        } finally {
            restore(connection);
        }
        throw new RuntimeException("Couldn't make transaction. Try again");
    }

    @Override
    public boolean deleteById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setLong(1, id);
            int rowsDelete = statement.executeUpdate();
            return rowsDelete == 1;
        } catch (SQLException e) {
            // FIXME add logging
        }
        return false;
    }

    @Override
    public List<Transaction> findAll(int limit, long offset) {
        List<Transaction> list = new ArrayList<>();
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            statement.setInt(1, limit);
            statement.setLong(2, offset);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(process(resultSet));
            }
            return list;
        } catch (SQLException e) {
            // FIXME add logging
        }
        return list;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(process(resultSet));
            }
        } catch (SQLException e) {
            // FIXME add logging
        }
        return Optional.empty();
    }

    private Transaction process(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setAccountId(rs.getLong("account_id"));
        transaction.setDestinationAccountId(rs.getLong("destination_account_id"));
        transaction.setAccountAmount(rs.getBigDecimal("account_amount"));
        transaction.setDestinationAccountAmount(rs.getBigDecimal("destination_account_amount"));
        transaction.setTransactionTime(rs.getTime("time").toInstant());
        return transaction;
    }

    private void restore(Connection connection) {
        try {
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            // FIXME add logging
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            // FIXME add logging
        }
    }
}
