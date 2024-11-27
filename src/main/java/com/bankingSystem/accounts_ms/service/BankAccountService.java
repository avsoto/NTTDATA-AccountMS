package com.bankingSystem.accounts_ms.service;

import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.AccountType;
import com.bankingSystem.accounts_ms.model.BankAccount;
import com.bankingSystem.accounts_ms.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link BankAccount} operations.
 * <p>
 * This service provides various methods to create, retrieve, update, and delete bank accounts.
 * It also includes methods for performing transactions such as deposits and withdrawals,
 * along with business logic for validation.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerIntegrationService customerIntegrationService;

    /**
     * Creates a new bank account after validating the associated customer.
     *
     * @param bankAccount the bank account to be created.
     * @return the created {@link BankAccount}.
     * @throws BusinessException if the customer associated with the account does not exist.
     */
    public BankAccount createAccount(BankAccount bankAccount) {
        return Optional.of(bankAccount)
                .filter(customerIntegrationService::isCustomerValid)
                .map(bankAccountRepository::save)
                .orElseThrow(() -> new BusinessException("Customer not found for ID: " + bankAccount.getCustomerId()));
    }

    /**
     * Retrieves all bank accounts.
     *
     * @return a list of all {@link BankAccount} objects.
     */
    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();
    }

    /**
     * Retrieves a bank account by its ID.
     *
     * @param accountId the ID of the bank account.
     * @return an {@link Optional} containing the {@link BankAccount} if found, or empty if not found.
     */
    public Optional<BankAccount> getAccountById(Integer accountId) {
        return bankAccountRepository.findById(accountId);
    }

    /**
     * Deletes a bank account by its ID.
     *
     * @param accountId the ID of the bank account to be deleted.
     * @return an {@link Optional} containing the deleted {@link BankAccount} if successful, or empty if not found.
     * @throws BusinessException if the account does not exist or there is an error during deletion.
     */
    public Optional<BankAccount> deleteAccountById(Integer accountId) {
        return bankAccountRepository.findById(accountId).map(existingAccount -> {
            try {
                bankAccountRepository.delete(existingAccount);
                return Optional.of(existingAccount);
            } catch (Exception e) {
                throw new BusinessException("Error deleting account with ID: " + accountId);
            }
        }).orElseThrow(() -> new BusinessException("Account not found for ID: " + accountId));
    }

    /**
     * Retrieves all bank accounts associated with a specific customer ID.
     *
     * @param customerId the ID of the customer.
     * @return a list of {@link BankAccount} objects associated with the given customer ID.
     */
    public List<BankAccount> getAccountsByCustomerId(Integer customerId) {
        return bankAccountRepository.findByCustomerId(customerId);
    }

    /**
     * Checks if a bank account exists for a given customer ID.
     *
     * @param customerId the ID of the customer.
     * @return true if an account exists for the given customer ID, otherwise false.
     */
    public boolean accountExists(Integer customerId) {
        return bankAccountRepository.existsByCustomerId(customerId);
    }

    /**
     * Updates the balance of a bank account.
     *
     * @param accountId the ID of the bank account.
     * @param newBalance the new balance to be set.
     * @return true if the balance was updated successfully, otherwise false.
     */
    public boolean updateBalance(Integer accountId, BigDecimal newBalance) {
        Optional<BankAccount> accountOpt = bankAccountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            account.setBalance(newBalance);
            bankAccountRepository.save(account);
            return true;
        }
        return false;
    }
}
