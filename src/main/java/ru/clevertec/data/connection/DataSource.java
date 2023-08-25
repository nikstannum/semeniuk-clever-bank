package ru.clevertec.data.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class DataSource implements AutoCloseable {

    private BlockingQueue<ProxyConnection> freeConnections;
    private Queue<ProxyConnection> givenAwayConnections;
    private int poolSize;
    public static final DataSource INSTANCE = new DataSource();

    private DataSource() {
        init();
    }

    public ProxyConnection getFreeConnections() {
        ProxyConnection connection = null;
        try {
            connection = freeConnections.take();
            givenAwayConnections.offer(connection);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e.getCause()); // FIXME add logging
        }
        return connection;
    }

    @SuppressWarnings("unchecked")
    private void init() {
        ConfigManager props = ConfigManager.INSTANCE;
        try {
            Map<String, Object> dbPropsMap = (Map<String, Object>) props.getProperty("db");
            Class.forName((String) dbPropsMap.get("driver"));
            Connection realConnection = DriverManager.getConnection((String) dbPropsMap.get("url"),
                    (String) dbPropsMap.get("user"), (String) dbPropsMap.get("password"));
            poolSize = (Integer) dbPropsMap.get("pool-size");
            freeConnections = new LinkedBlockingDeque<>(poolSize);
            givenAwayConnections = new ArrayDeque<>();
            for (int i = 0; i < poolSize; i++) {
                freeConnections.add(new ProxyConnection(realConnection));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e.getCause()); // FIXME add logging
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e.getCause()); // FIXME add logging
        }
    }

    public void releaseConnection(ProxyConnection connection) {
        givenAwayConnections.remove(connection);
        freeConnections.offer(connection);
    }

    private void destroyPoll() {
        try {
            for (int i = 0; i < poolSize; i++) {
                freeConnections.take().reallyClose();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e.getCause()); // FIXME add logging
        }
    }

    @Override
    public void close() throws Exception {
        destroyPoll();
        DriverManager.getDrivers().asIterator().forEachRemaining(driver -> {
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e.getCause()); // FIXME add logging
            }
        });
    }

}
