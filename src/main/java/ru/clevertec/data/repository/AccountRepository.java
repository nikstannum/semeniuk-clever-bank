package ru.clevertec.data.repository;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import ru.clevertec.data.entity.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
    /**
     * returns a paginated list of accounts with a balance greater than zero
     *
     * @param limit      the list size
     * @param offset     number of objects behind the list
     * @param connection database
     * @return the list of objects
     */
    List<Account> findAllAmountMoreZero(int limit, long offset, Connection connection);

    /**
     * method to get the number of accounts with a balance greater than zero
     *
     * @param connection database
     * @return number of accounts
     */
    Long countAccountWithAmountMoreZero(Connection connection);

    /**
     * updates the account by its number
     *
     * @param account    actual account
     * @param connection database
     */
    void updateAmountByNumber(Account account, Connection connection);

    /**
     * returns the account by its number
     *
     * @param number account number
     * @return the object placed in an {@link java.util.Optional} container. Can be empty.
     */
    Optional<Account> findByNumber(String number);
}
