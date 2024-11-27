package com.bankingSystem.accounts_ms.repository;

import com.bankingSystem.accounts_ms.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on {@link BankAccount} entities.
 * <p>
 * This interface extends JpaRepository, providing standard database operations such as
 * saving, deleting, and querying bank account data. Additionally, custom query methods
 * are defined to check for the existence of accounts by customer ID and to find all
 * accounts associated with a specific customer.
 * </p>
 */
public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {

    /**
     * Checks if a bank account exists for a given customer ID.
     *
     * @param customerId the ID of the customer.
     * @return true if a bank account exists for the given customer ID, otherwise false.
     */
    boolean existsByCustomerId(Integer customerId);

    /**
     * Finds all bank accounts associated with a specific customer ID.
     *
     * @param customerId the ID of the customer.
     * @return a list of {@link BankAccount} objects associated with the given customer ID.
     */
    List<BankAccount> findByCustomerId(Integer customerId);
}
