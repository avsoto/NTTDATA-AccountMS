package com.bankingSystem.accounts_ms.controller;

import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.BankAccount;
import com.bankingSystem.accounts_ms.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling bank account operations.
 * <p>
 * This class exposes several endpoints for creating, retrieving, updating, and deleting bank accounts.
 * It uses the {@link BankAccountService} to interact with the business logic.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    /**
     * Creates a new bank account.
     *
     * @param bankAccount the bank account object to be created.
     * @return ResponseEntity with the created account and HTTP status 201 (CREATED).
     */
    @PostMapping
    public ResponseEntity<BankAccount> createAccount(@RequestBody BankAccount bankAccount) {
        BankAccount createdAccount = bankAccountService.createAccount(bankAccount);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    /**
     * Retrieves all bank accounts.
     *
     * @return ResponseEntity with the list of bank accounts and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<?> getAllAccounts() {
        List<BankAccount> accounts = bankAccountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Retrieves a bank account by its ID.
     *
     * @param accountId the ID of the bank account.
     * @return ResponseEntity with the account if found, or HTTP status 404 (NOT FOUND) if not found.
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable Integer accountId) {
        Optional<BankAccount> account = bankAccountService.getAccountById(accountId);
        return account.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deposits an amount into a bank account.
     *
     * @param accountId the ID of the bank account.
     * @param amount    the amount to deposit.
     * @return ResponseEntity with the updated account and HTTP status 200 (OK).
     */
    @PutMapping("/{accountId}/deposit")
    public ResponseEntity<BankAccount> deposit(@PathVariable Integer accountId, @RequestParam BigDecimal amount) {
        try {
            BankAccount updatedAccount = bankAccountService.deposit(accountId, amount);
            return ResponseEntity.status(HttpStatus.OK).body(updatedAccount);
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Withdraws an amount from a bank account.
     *
     * @param accountId the ID of the bank account.
     * @param amount    the amount to withdraw.
     * @return ResponseEntity with the updated account and HTTP status 200 (OK).
     */
    @PutMapping("/{accountId}/withdrawal")
    public ResponseEntity<BankAccount> withdraw(@PathVariable Integer accountId, @RequestParam BigDecimal amount) {
        try {
            BankAccount updatedAccount = bankAccountService.withdraw(accountId, amount);
            return ResponseEntity.status(HttpStatus.OK).body(updatedAccount);
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Deletes a bank account by its ID.
     *
     * @param accountId the ID of the bank account to be deleted.
     * @return ResponseEntity with the deleted account or HTTP status 404 (NOT FOUND) if not found.
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> deleteAccountById(@PathVariable Integer accountId) {
        Optional<BankAccount> deletedAccount = bankAccountService.deleteAccountById(accountId);
        return deletedAccount
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Checks if a customer has any bank account.
     *
     * @param customerId the ID of the customer.
     * @return true if there is at least one bank account for the customer, false otherwise.
     */
    @GetMapping("/customer/{customerId}")
    public boolean getAccountByCustomerId(@PathVariable Integer customerId) {
        return bankAccountService.accountExists(customerId);
    }

    /**
     * Checks if a customer has any active accounts.
     *
     * @param customerId the ID of the customer.
     * @return ResponseEntity with a boolean indicating whether the customer has active accounts or not.
     */
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<Boolean> hasActiveAccounts(@PathVariable Integer customerId) {
        List<BankAccount> accounts = bankAccountService.getAccountsByCustomerId(customerId);
        if (accounts == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }

        return ResponseEntity.ok(!accounts.isEmpty());
    }

    /**
     * Updates the balance of a bank account.
     *
     * @param accountId the ID of the bank account to be updated.
     * @param body      a map containing the new balance under the key "balance".
     * @return ResponseEntity with HTTP status 200 (OK) if updated successfully or HTTP status 404 (NOT FOUND) if the account is not found.
     */
    @PutMapping("/{accountId}/balance")
    public ResponseEntity<?> updateBalance(@PathVariable Integer accountId, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal newBalance = body.get("balance");

        boolean updated = bankAccountService.updateBalance(accountId, newBalance);

        if (updated) {
            System.out.println("Balance successfully updated to the account" + accountId + " with new balance: " + newBalance);
            return ResponseEntity.ok().build();
        } else {
            System.out.println("Could not update balance for account" + accountId);
            return ResponseEntity.notFound().build();
        }
    }

}

