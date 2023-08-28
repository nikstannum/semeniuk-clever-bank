package ru.clevertec.data.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.AccountRepository;
import ru.clevertec.service.exception.NotFoundException;

@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private static final String FIND_BY_ID = """
            SELECT a.id, a."number", a.amount, a.open_time,
            u.id AS user_id, u.first_name, u.last_name, u.email,
            b.id AS bank_id, b."name", b.bank_identifier,
            c."name" AS currency
            FROM accounts a
            JOIN users u  ON u.id = a.user_id
            JOIN banks b ON a.bank_id = b.id
            JOIN currencies c ON a.currency_id = c.id
            WHERE a.id = ? AND a.deleted = false
            """;
    private static final String FIND_ALL = """
            SELECT a.id, a."number", a.amount, a.open_time,
            u.id AS user_id, u.first_name, u.last_name, u.email,
            b.id AS bank_id, b."name", b.bank_identifier,
            c."name" AS currency
            FROM accounts a
            JOIN users u  ON u.id = a.user_id
            JOIN banks b ON a.bank_id = b.id
            JOIN currencies c ON a.currency_id = c.id
            WHERE a.deleted = false
            ORDER BY a.id
            LIMIT ?
            OFFSET ?
            """;

    private static final String COUNT_ACCOUNT_AMOUNT_MORE_ZERO = """
            SELECT count(a.id) AS total  FROM accounts a
            WHERE a.amount > 0
            AND a.deleted = false
            """;
    private static final String DELETE_BY_ID = """
            UPDATE accounts
            SET deleted = true
            WHERE id = ?
            """;
    private static final String DELETE_BY_NUMBER = """
            UPDATE accounts
            SET deleted = true
            WHERE "number" = ?
            """;
    private static final String CREATE_ACCOUNT = """
            INSERT INTO accounts(user_id, bank_id, amount, currency_id)
            VALUES
            ((SELECT id FROM users u WHERE u.email = ? AND u.deleted = false),
            (SELECT id FROM banks b WHERE b.bank_identifier = ? AND b.deleted = false),
            0,
            (SELECT id FROM currencies WHERE name = ?))
            """;
    private static final String UPDATE_ACCOUNT_BY_ID = """
            UPDATE accounts
            SET bank_id = (SELECT b.id FROM banks b WHERE b.bank_identifier = ? AND b.deleted = false),
            amount = ?
            WHERE id = ?
            """;
    private static final String FIND_BY_NUMBER = """
            SELECT a.id, a."number", a.amount, a.open_time,
            u.id AS user_id, u.first_name, u.last_name, u.email,
            b.id AS bank_id, b."name", b.bank_identifier,
            c."name" AS currency
            FROM accounts a
            JOIN users u  ON u.id = a.user_id
            JOIN banks b ON a.bank_id = b.id
            JOIN currencies c ON a.currency_id = c.id
            WHERE a."number" = ? AND a.deleted = false
            """;
    private static final String UPDATE_AMOUNT_BY_NUMBER = """
            UPDATE accounts
            SET amount = ?
            WHERE "number" = ?
            """;
    private static final String INCREASE_AMOUNT_BY_ID = """
            UPDATE accounts
            SET amount = ?
            WHERE id = ?
            """;

    private static final String FIND_ALL_AMOUNT_MORE_ZERO = """
            SELECT a.id, a."number", a.amount, a.open_time
            FROM accounts a
            WHERE a.deleted = false AND a.amount > 0
            ORDER BY a.id
            LIMIT ?
            OFFSET ?
            """;

    private final DataSource dataSource;

    @Override
    public List<Account> findAllAmountMoreZero(int limit, long offset, Connection connection) {
        List<Account> list = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(FIND_ALL_AMOUNT_MORE_ZERO);
            statement.setInt(1, limit);
            statement.setLong(2, offset);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(processLazy(resultSet));
            }
            return list;
        } catch (
                SQLException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
    }

    private Account processLazy(ResultSet resultSet) throws SQLException {
        Account account = new Account();
        account.setId(resultSet.getLong("id"));
        account.setNumber(resultSet.getString("number"));
        account.setAmount(resultSet.getBigDecimal("amount"));
        account.setOpenTime(resultSet.getDate("open_time").toLocalDate());
        return account;
    }

    @Override
    public void increaseAmountById(Account account, Connection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement(INCREASE_AMOUNT_BY_ID);
            statement.setBigDecimal(1, account.getAmount());
            statement.setLong(2, account.getId());
            int rowUpd = statement.executeUpdate();
            if (rowUpd != 1) {
                throw new RuntimeException("Error calculating interest for account ID = " + account.getId() + " Updated " + rowUpd + " rows");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long countAccountWithAmountMoreZero(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(COUNT_ACCOUNT_AMOUNT_MORE_ZERO);
            if (rs.next()) {
                return rs.getLong("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("count of accounts not definition");
    }

    @Override
    public void updateAmountByNumber(Account account, Connection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_AMOUNT_BY_NUMBER);
            statement.setBigDecimal(1, account.getAmount());
            statement.setString(2, account.getNumber());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Account> findByNumber(String number) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_NUMBER)) {
            statement.setString(1, number);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(processEager(resultSet));
            }
        } catch (SQLException e) {
            // FIXME add logging
        }
        return Optional.empty();
    }

    @Override
    public Account create(Account entity) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(CREATE_ACCOUNT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getUser().getEmail());
            statement.setString(2, entity.getBank().getBankIdentifier());
            statement.setString(3, entity.getCurrency().toString());
            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                Long id = keys.getLong("id");
                return findById(id).orElseThrow(() -> new NotFoundException("Couldn't find account after its creation"));
            }
        } catch (SQLException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Optional<Account> findById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(processEager(resultSet));
            }
        } catch (SQLException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Account processEager(ResultSet resultSet) throws SQLException {
        Account account = new Account();
        account.setId(resultSet.getLong("id"));
        account.setNumber(resultSet.getString("number"));
        account.setAmount(resultSet.getBigDecimal("amount"));
        account.setOpenTime(resultSet.getDate("open_time").toLocalDate());
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        account.setUser(user);
        Bank bank = new Bank();
        bank.setId(resultSet.getLong("bank_id"));
        bank.setName(resultSet.getString("name"));
        bank.setBankIdentifier(resultSet.getString("bank_identifier"));
        account.setBank(bank);
        account.setCurrency(Currency.valueOf(resultSet.getString("currency")));
        return account;
    }

    @Override
    public List<Account> findAll(int limit, long offset) {
        List<Account> list = new ArrayList<>();
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            statement.setInt(1, limit);
            statement.setLong(2, offset);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(processEager(resultSet));
            }
            return list;
        } catch (SQLException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
    }

    @Override
    public Account update(Account entity) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ACCOUNT_BY_ID)) {
            statement.setString(1, entity.getBank().getBankIdentifier());
            statement.setBigDecimal(2, entity.getAmount());
            statement.setLong(3, entity.getId());
            statement.executeUpdate();
            return findById(entity.getId()).orElseThrow(() -> new NotFoundException("Couldn't find account after its updating"));
        } catch (SQLException e) {
            // FIXME add logging
        }
        return null;
    }

    @Override
    public void deleteById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setLong(1, id);
            int rowsDelete = statement.executeUpdate();
            if (rowsDelete != 1) {
                throw new RuntimeException("Error when deleting account. The change affected " + rowsDelete + " rows");
            }
        } catch (SQLException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByNumber(String number) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_NUMBER)) {
            statement.setString(1, number);
            int rowsDelete = statement.executeUpdate();
            if (rowsDelete != 1) {
                throw new RuntimeException("Error when deleting account. The change affected " + rowsDelete + " rows");
            }
        } catch (SQLException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
    }
}
