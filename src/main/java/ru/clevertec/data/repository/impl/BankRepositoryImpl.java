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
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.repository.BankRepository;

@RequiredArgsConstructor
public class BankRepositoryImpl implements BankRepository {

    private static final String CREATE_BANK = """
            INSERT INTO banks ("name", bank_identifier)
            VALUES (?, ?)
            """;
    private static final String FIND_BY_ID = """
            SELECT b.id, b."name", b.bank_identifier
            FROM banks b
            WHERE b.id = ? AND b.deleted = false;
            """;
    private static final String FIND_BANK_BY_IDENTIFIER = """
            SELECT b.id, b."name", b.bank_identifier
            FROM banks b
            WHERE b.bank_identifier = ? AND b.deleted = false;
            """;
    private static final String FIND_ALL = """
            SELECT b.id, b."name", b.bank_identifier
            FROM banks b
            WHERE b.deleted = false
            ORDER by b.id
            LIMIT ?
            OFFSET ?
            """;
    private static final String DELETE_BANK_BY_ID = """
            UPDATE banks
            SET deleted = true
            WHERE id = ?;
            """;
    private static final String UPDATE_BANK = """
            UPDATE banks
            SET "name" = ?, bank_identifier = ?
            WHERE id = ?;
            """;
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_BANK_IDENTIFIER = "bank_identifier";
    private final DataSource dataSource;

    @Override
    public Bank update(Bank entity) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(UPDATE_BANK)) {
            statement.setString(1, entity.getName());
            statement.setString(2, entity.getBankIdentifier());
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
             PreparedStatement statement = connection.prepareStatement(DELETE_BANK_BY_ID)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Bank> findAll(int limit, long offset) {
        List<Bank> list = new ArrayList<>();
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Bank> findByIdentifier(String identifier) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_BANK_BY_IDENTIFIER)) {
            statement.setString(1, identifier);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(process(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Bank> findById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(process(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Bank process(ResultSet resultSet) throws SQLException {
        Bank bank = new Bank();
        bank.setId(resultSet.getLong(COLUMN_ID));
        bank.setName(resultSet.getString(COLUMN_NAME));
        bank.setBankIdentifier(resultSet.getString(COLUMN_BANK_IDENTIFIER));
        return bank;
    }

    @Override
    public Bank create(Bank entity) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(CREATE_BANK, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName());
            statement.setString(2, entity.getBankIdentifier());
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
}
