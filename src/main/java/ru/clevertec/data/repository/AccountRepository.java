package ru.clevertec.data.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ru.clevertec.data.entity.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
    void deleteByNumber(String number);

    List<Account> findAllAmountMoreZero(int limit, long offset, Connection connection);

    Long countAccountWithAmountMoreZero(Connection connection);

    void increaseAmountById(Account account, Connection connection);

    void updateAmountByNumber(Account account, Connection connection);


    Optional<Account> findByNumber(String number);
}
