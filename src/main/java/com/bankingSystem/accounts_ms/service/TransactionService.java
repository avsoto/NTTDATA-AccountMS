package com.bankingSystem.accounts_ms.service;

import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.AccountType;
import com.bankingSystem.accounts_ms.model.BankAccount;
import com.bankingSystem.accounts_ms.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
/**
 * Service class for managing financial transactions (deposit and withdrawal) in bank accounts.
 * <p>
 * This service provides methods to perform deposit and withdrawal operations on bank accounts,
 * including validations for account balance, account type, and overdraft limits.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final BankAccountRepository bankAccountRepository;

    /**
     * Deposits a specified amount into a bank account.
     * <p>
     * This method checks if the deposit amount is positive, finds the bank account by ID, and updates
     * the balance by adding the deposit amount. The updated account is then saved.
     * </p>
     *
     * @param accountId the ID of the bank account.
     * @param amount the amount to be deposited.
     * @return the updated {@link BankAccount} after the deposit.
     * @throws BusinessException if the deposit amount is zero or negative, or if the account does not exist.
     */
    public BankAccount deposit(Integer accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Deposit amount must be greater than zero.");
        }

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found for ID: " + accountId));

        BigDecimal newBalance = bankAccount.getBalance().add(amount);
        bankAccount.setBalance(newBalance);

        return bankAccountRepository.save(bankAccount);
    }

    /**
     * Withdraws a specified amount from a bank account.
     * <p>
     * This method checks if the withdrawal amount is positive, finds the bank account by ID, validates
     * the withdrawal conditions (such as account type and available balance), and updates the balance
     * by subtracting the withdrawal amount. The updated account is then saved.
     * </p>
     *
     * @param accountId the ID of the bank account.
     * @param amount the amount to be withdrawn.
     * @return the updated {@link BankAccount} after the withdrawal.
     * @throws BusinessException if the withdrawal amount is zero or negative, if the account does not exist,
     *                           or if there are insufficient funds in the account.
     */
    public BankAccount withdraw(Integer accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Withdrawal amount must be greater than zero.");
        }

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found for ID: " + accountId));

        validateWithdrawal(bankAccount, amount);

        BigDecimal newBalance = bankAccount.getBalance().subtract(amount);
        bankAccount.setBalance(newBalance);

        return bankAccountRepository.save(bankAccount);
    }

    /**
     * Validates the withdrawal conditions for a bank account.
     * <p>
     * This method ensures that the withdrawal amount does not exceed the available balance or
     * the overdraft limit for the given account type. For savings accounts, the balance must
     * be sufficient. For checking accounts, the balance must not go below the overdraft limit.
     * </p>
     *
     * @param account the bank account to validate.
     * @param amount the amount to be withdrawn.
     * @throws BusinessException if the withdrawal exceeds the available balance or overdraft limit.
     */
    private void validateWithdrawal(BankAccount account, BigDecimal amount) {
        if (account.getAccountType() == AccountType.SAVINGS && account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance for withdrawal in savings account.");
        }

        if (account.getAccountType() == AccountType.CHECKING && account.getBalance().subtract(amount).compareTo(new BigDecimal("-500")) < 0) {
            throw new BusinessException("Withdrawal exceeds overdraft limit for checking account.");
        }
    }
}
