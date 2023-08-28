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
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NUMBER = "number";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_OPEN_TIME = "open_time";
    private static final String ERROR_CALCULATING_INTEREST_FOR_ACCOUNT = "Error calculating interest for account ID = %d. Updated %d rows";
    private static final String COLUMN_TOTAL = "total";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_BANK_ID = "bank_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_BANK_IDENTIFIER = "bank_identifier";
    private static final String COLUMN_CURRENCY = "currency";

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Account processLazy(ResultSet resultSet) throws SQLException {
        Account account = new Account();
        account.setId(resultSet.getLong(COLUMN_ID));
        account.setNumber(resultSet.getString(COLUMN_NUMBER));
        account.setAmount(resultSet.getBigDecimal(COLUMN_AMOUNT));
        account.setOpenTime(resultSet.getDate(COLUMN_OPEN_TIME).toLocalDate());
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
                throw new RuntimeException(String.format(ERROR_CALCULATING_INTEREST_FOR_ACCOUNT, account.getId(), rowUpd));
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
            rs.next();
            return rs.getLong(COLUMN_TOTAL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
            throw new RuntimeException(e);
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
            keys.next();
            Long id = keys.getLong(COLUMN_ID);
            entity.setId(id);
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Account processEager(ResultSet resultSet) throws SQLException {
        Account account = new Account();
        account.setId(resultSet.getLong(COLUMN_ID));
        account.setNumber(resultSet.getString(COLUMN_NUMBER));
        account.setAmount(resultSet.getBigDecimal(COLUMN_AMOUNT));
        account.setOpenTime(resultSet.getDate(COLUMN_OPEN_TIME).toLocalDate());
        User user = new User();
        user.setId(resultSet.getLong(COLUMN_USER_ID));
        user.setFirstName(resultSet.getString(COLUMN_FIRST_NAME));
        user.setLastName(resultSet.getString(COLUMN_LAST_NAME));
        user.setEmail(resultSet.getString(COLUMN_EMAIL));
        account.setUser(user);
        Bank bank = new Bank();
        bank.setId(resultSet.getLong(COLUMN_BANK_ID));
        bank.setName(resultSet.getString(COLUMN_NAME));
        bank.setBankIdentifier(resultSet.getString(COLUMN_BANK_IDENTIFIER));
        account.setBank(bank);
        account.setCurrency(Currency.valueOf(resultSet.getString(COLUMN_CURRENCY)));
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
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByNumber(String number) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_NUMBER)) {
            statement.setString(1, number);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
