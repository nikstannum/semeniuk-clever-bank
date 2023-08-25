package ru.clevertec.service.factory;

import java.util.HashMap;
import java.util.Map;
import ru.clevertec.data.factory.DaoFactory;
import ru.clevertec.data.repository.AccountRepository;
import ru.clevertec.data.repository.BankRepository;
import ru.clevertec.data.repository.TransactionRepository;
import ru.clevertec.data.repository.UserRepository;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.TransactionService;
import ru.clevertec.service.impl.AccountServiceImpl;
import ru.clevertec.service.impl.TransactionServiceImpl;

public class ServiceFactory {

    public static final ServiceFactory INSTANCE = new ServiceFactory();
    private final Map<Class<?>, Object> map;

    private ServiceFactory() {
        this.map = new HashMap<>();
        map.put(AccountService.class, new AccountServiceImpl(DaoFactory.INSTANCE.getDao(AccountRepository.class),
                DaoFactory.INSTANCE.getDao(UserRepository.class), DaoFactory.INSTANCE.getDao(BankRepository.class)));
        map.put(TransactionService.class, new TransactionServiceImpl(DaoFactory.INSTANCE.getDao(TransactionRepository.class),
                DaoFactory.INSTANCE.getDao(AccountRepository.class)));
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<?> clazz) {
        return (T) map.get(clazz);
    }
}
