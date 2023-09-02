package ru.clevertec.data;

import java.sql.Connection;
import java.util.function.Consumer;

public interface DbTransactionManager {
    void execute(Consumer<Connection> consumer);
}
