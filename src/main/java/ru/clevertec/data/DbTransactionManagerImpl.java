package ru.clevertec.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.connection.DataSource;

@RequiredArgsConstructor
public class DbTransactionManagerImpl implements DbTransactionManager {

    private final DataSource dataSource;

    @Override
    public void execute(Consumer<Connection> consumer) {
        Connection connection = dataSource.getFreeConnections();
        try {
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw new RuntimeException(e);
        } finally {
            restore(connection);
        }
    }

    private void restore(Connection connection) {
        try {
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
