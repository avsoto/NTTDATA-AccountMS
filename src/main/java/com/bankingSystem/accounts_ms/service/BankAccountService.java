package com.bankingSystem.accounts_ms.service;

import com.bankingSystem.accounts_ms.exceptions.BusinessException;
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


    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();
    }

    public Optional<BankAccount> getAccountById(Integer accountId) {
        return bankAccountRepository.findById(accountId);
    }

    public List<BankAccount> getAccountsByCustomerId(Integer customerId) {
        return bankAccountRepository.findByCustomerId(customerId);
    }

    public boolean accountExists(Integer customerId) {
        return bankAccountRepository.existsByCustomerId(customerId);
    }

    public BankAccount createAccount(BankAccount bankAccount) {
        return Optional.of(bankAccount)
                .filter(this::isCustomerValid)
                .map(bankAccountRepository::save)
                .orElseThrow(() -> new BusinessException("Customer not found for ID: " + bankAccount.getCustomerId()));
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
