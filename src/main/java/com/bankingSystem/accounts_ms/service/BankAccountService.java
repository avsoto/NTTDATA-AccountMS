package com.bankingSystem.accounts_ms.service;

import com.bankingSystem.accounts_ms.dto.BankAccountDTO;
import com.bankingSystem.accounts_ms.dto.BankAccountMapper;
import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.BankAccount;
import com.bankingSystem.accounts_ms.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
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
     * <p>
     * This method checks if the customer associated with the bank account is valid by calling the
     * {@link CustomerIntegrationService}. If the customer is valid, the account is saved to the repository.
     * Otherwise, a {@link BusinessException} is thrown.
     * </p>
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
     * <p>
     * This method returns all bank accounts available in the repository, converting them into {@link BankAccountDTO} objects
     * using the {@link BankAccountMapper}.
     * </p>
     *
     * @return a list of all {@link BankAccountDTO} objects.
     */
    public List<BankAccountDTO> getAllAccounts() {
        List<BankAccount> accounts = bankAccountRepository.findAll();
        return BankAccountMapper.toDTOList(accounts);
    }

    /**
     * Retrieves a bank account by its ID.
     * <p>
     * This method returns the bank account with the given ID, if it exists, mapped to a {@link BankAccountDTO}.
     * </p>
     *
     * @param accountId the ID of the bank account.
     * @return an {@link Optional} containing the {@link BankAccountDTO} if found, or empty if not found.
     */
    public Optional<BankAccountDTO> getAccountById(Integer accountId) {
        return bankAccountRepository.findById(accountId)
                .map(BankAccountMapper::toDTO);
    }

    /**
     * Deletes a bank account by its ID.
     * <p>
     * This method deletes the account with the given ID from the repository. If successful, it returns the deleted account
     * mapped to a {@link BankAccountDTO}. If the account is not found, a {@link BusinessException} is thrown.
     * </p>
     *
     * @param accountId the ID of the bank account to be deleted.
     * @return an {@link Optional} containing the deleted {@link BankAccountDTO} if successful, or empty if not found.
     * @throws BusinessException if the account does not exist or there is an error during deletion.
     */
    public Optional<BankAccountDTO> deleteAccountById(Integer accountId) {
        return bankAccountRepository.findById(accountId).map(existingAccount -> {
            try {
                bankAccountRepository.delete(existingAccount);
                return Optional.of(BankAccountMapper.toDTO(existingAccount));
            } catch (Exception e) {
                throw new BusinessException("Error deleting account with ID: " + accountId);
            }
        }).orElseThrow(() -> new BusinessException("Account not found for ID: " + accountId));
    }

    /**
     * Retrieves all bank accounts associated with a specific customer ID.
     * <p>
     * This method returns a list of {@link BankAccountDTO} objects for all bank accounts linked to a given customer ID.
     * If no accounts are found, it returns an empty list.
     * </p>
     *
     * @param customerId the ID of the customer.
     * @return a list of {@link BankAccountDTO} objects associated with the given customer ID.
     */
    public List<BankAccountDTO> getAccountsByCustomerId(Integer customerId) {
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customerId);
        return accounts == null ? Collections.emptyList() : BankAccountMapper.toDTOList(accounts);
    }

    /**
     * Checks if a bank account exists for a given customer ID.
     * <p>
     * This method returns true if at least one bank account is associated with the given customer ID.
     * Otherwise, it returns false.
     * </p>
     *
     * @param customerId the ID of the customer.
     * @return true if an account exists for the given customer ID, otherwise false.
     */
    public boolean accountExists(Integer customerId) {
        return bankAccountRepository.existsByCustomerId(customerId);
    }

    /**
     * Updates the balance of a bank account.
     * <p>
     * This method sets a new balance for the bank account with the specified ID. If the account is found and the balance is updated,
     * it returns true. If the account does not exist, it returns false.
     * </p>
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
