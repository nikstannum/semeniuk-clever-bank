package ru.clevertec.data.factory;

import java.util.HashMap;
import java.util.Map;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.data.repository.AccountRepository;
import ru.clevertec.data.repository.BankRepository;
import ru.clevertec.data.repository.CrudRepository;
import ru.clevertec.data.repository.TransactionRepository;
import ru.clevertec.data.repository.UserRepository;
import ru.clevertec.data.repository.impl.AccountRepositoryImpl;
import ru.clevertec.data.repository.impl.BankRepositoryImpl;
import ru.clevertec.data.repository.impl.TransactionRepositoryImpl;
import ru.clevertec.data.repository.impl.UserRepositoryImpl;

public class DaoFactory {

    public static final DaoFactory INSTANCE = new DaoFactory();
    private final Map<Class<?>, CrudRepository<?, ?>> map;

    private DaoFactory() {
        DataSource dataSource = DataSource.INSTANCE;
        this.map = new HashMap<>();
        map.put(AccountRepository.class, new AccountRepositoryImpl(dataSource));
        map.put(BankRepository.class, new BankRepositoryImpl(dataSource));
        map.put(TransactionRepository.class, new TransactionRepositoryImpl(dataSource));
        map.put(UserRepository.class, new UserRepositoryImpl(dataSource));
    }

    @SuppressWarnings("unchecked")
    public <T> T getDao(Class<T> clazz) {
        return (T) map.get(clazz);
    }
}
