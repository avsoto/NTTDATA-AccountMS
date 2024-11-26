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

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final RestTemplate restTemplate;

    @Value("${customer.ms.url}")
    private String customerMicroserviceUrl;

    public BankAccount createAccount(BankAccount bankAccount) {
        return Optional.of(bankAccount)
                .filter(this::isCustomerValid)
                .map(bankAccountRepository::save)
                .orElseThrow(() -> new BusinessException("Customer not found for ID: " + bankAccount.getCustomerId()));
    }

    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();
    }

    public Optional<BankAccount> getAccountById(Integer accountId) {
        return bankAccountRepository.findById(accountId);
    }

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

    private void validateWithdrawal(BankAccount account, BigDecimal amount) {
        if (account.getAccountType() == AccountType.SAVINGS && account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance for withdrawal in savings account.");
        }

        if (account.getAccountType() == AccountType.CHECKING && account.getBalance().subtract(amount).compareTo(new BigDecimal("-500")) < 0) {
            throw new BusinessException("Withdrawal exceeds overdraft limit for checking account.");
        }
    }

    public Optional<BankAccount> deleteAccountById(Integer accountId) {
        return bankAccountRepository.findById(accountId).map(existingAccount -> {
            try {
                bankAccountRepository.delete(existingAccount);
                return existingAccount;
            } catch (Exception e) {
                throw new BusinessException("Error deleting account with ID: " + accountId + e);
            }
        });
    }

    public List<BankAccount> getAccountsByCustomerId(Integer customerId) {
        return bankAccountRepository.findByCustomerId(customerId);
    }

    public boolean accountExists(Integer customerId) {
        return bankAccountRepository.existsByCustomerId(customerId);
    }

    private boolean isCustomerValid(BankAccount bankAccount) {
        return customerExists(bankAccount.getCustomerId());
    }

    private boolean customerExists(Integer customerId) {
        String url = customerMicroserviceUrl + "/" + customerId;

        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.out.println("Error calling customer microservice for ID: {}" + customerId + e.getMessage());
        }
        throw new BusinessException("Customer with ID " + customerId + " not found.");
    }

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
