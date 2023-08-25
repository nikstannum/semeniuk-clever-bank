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
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.UserRepository;
import ru.clevertec.service.exception.NotFoundException;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final DataSource dataSource;

    private static final String FIND_USER_BY_EMAIL = """
            SELECT u.id, u.first_name, u.last_name, u.email
            FROM users u
            WHERE u.email = ? AND u.deleted = false
            """;

    private static final String CREATE_USER = """
            INSERT INTO users (first_name, last_name, email)
            VALUES (?, ?, ?)
            """;

    private static final String FIND_USER_BY_ID = """
            SELECT u.id, u.first_name, u.last_name, u.email
            FROM users u
            WHERE u.id  = ? AND u.deleted = false
            """;

    private static final String FIND_ALL = """
            SELECT u.id, u.first_name, u.last_name, u.email
            FROM users u
            WHERE u.deleted = false
            ORDER by u.id
            LIMIT ?
            OFFSET ?
            """;

    private static final String UPDATE_USER = """
            UPDATE users
            SET first_name = ?, last_name = ?, email = ?
            WHERE id = ?
            """;

    private static final String DELETE_USER_BY_ID = """
            UPDATE users
            SET deleted = true
            WHERE id = ?
            """;

    @Override
    public boolean deleteById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(DELETE_USER_BY_ID)) {
            statement.setLong(1, id);
            int rowsDelete = statement.executeUpdate();
            return rowsDelete == 1;
        } catch (SQLException e) {
            // FIXME add logging
        }
        return false;
    }

    @Override
    public User update(User entity) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USER)) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getEmail());
            statement.setLong(4, entity.getId());
            statement.executeUpdate();
            return findById(entity.getId()).orElseThrow(() -> new NotFoundException("Couldn't find account after its updating"));
        } catch (SQLException e) {
            // FIXME add logging
        }
        return null;
    }

    @Override
    public List<User> findAll(int limit, long offset) {
        List<User> list = new ArrayList<>();
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
    public Optional<User> findById(Long id) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_USER_BY_ID)) {
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

    @Override
    public User create(User entity) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getEmail());
            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                Long id = keys.getLong("id");
                return findById(id).orElseThrow(() -> new NotFoundException("Couldn't find user after its creation"));
            }
        } catch (SQLException e) {
            // FIXME add logging
        }
        return null;
    }

    private User process(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        return user;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        try (Connection connection = dataSource.getFreeConnections();
             PreparedStatement statement = connection.prepareStatement(FIND_USER_BY_EMAIL)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(process(resultSet));
            }
        } catch (SQLException e) {
            // FIXME add logging
        }
        return Optional.empty();
    }
}
