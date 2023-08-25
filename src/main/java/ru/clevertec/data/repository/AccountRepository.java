package ru.clevertec.data.repository;

import java.util.Optional;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Transaction;

public interface AccountRepository extends CrudRepository<Account, Long> {
    boolean deleteByNumber(String number);

    Optional<Account> findByNumber(String number);
}
