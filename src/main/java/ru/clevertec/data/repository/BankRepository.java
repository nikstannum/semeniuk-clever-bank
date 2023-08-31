package ru.clevertec.data.repository;

import java.util.Optional;
import ru.clevertec.data.entity.Bank;

public interface BankRepository extends CrudRepository<Bank, Long> {
    /**
     * method to get an object by its identifier
     *
     * @param identifier object identifier
     * @return the object placed in an {@link java.util.Optional} container. Can be empty.
     */
    Optional<Bank> findByIdentifier(String identifier);
}
