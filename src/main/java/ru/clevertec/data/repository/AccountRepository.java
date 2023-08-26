package ru.clevertec.data.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ru.clevertec.data.entity.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
    boolean deleteByNumber(String number);

    Map<Long, BigDecimal> findAllAmountMoreZero(int limit, long offset);

    Long countAccountWithAmountMoreZero();

    void increaseAmountById(Map<Long, BigDecimal> map, Connection connection);

    void updateAmountByNumber(Account account, Connection connection);


    Optional<Account> findByNumber(String number);
}
