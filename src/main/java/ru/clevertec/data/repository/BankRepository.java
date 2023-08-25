package ru.clevertec.data.repository;

import java.util.Optional;
import ru.clevertec.data.entity.Bank;

public interface BankRepository extends CrudRepository<Bank, Long> {
    Optional<Bank> findByIdentifier(String identifier);
}
