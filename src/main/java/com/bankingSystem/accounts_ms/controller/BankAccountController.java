package com.bankingSystem.accounts_ms.controller;

import com.bankingSystem.accounts_ms.dto.BankAccountDTO;
import com.bankingSystem.accounts_ms.dto.BankAccountMapper;
import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.BankAccount;
import com.bankingSystem.accounts_ms.service.BankAccountService;
import com.bankingSystem.accounts_ms.service.TransactionService;
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
    private final TransactionService transactionService;

    /**
     * Creates a new bank account.
     *
     * @param bankAccountDTO the bank account object to be created.
     * @return ResponseEntity with the created account and HTTP status 201 (CREATED).
     */
    @PostMapping
    public ResponseEntity<BankAccountDTO> createAccount(@RequestBody BankAccountDTO bankAccountDTO) {
        BankAccount createdAccount = bankAccountService.createAccount(BankAccountMapper.fromDTO(bankAccountDTO));
        BankAccountDTO createdAccountDTO = BankAccountMapper.toDTO(createdAccount);

        return new ResponseEntity<>(createdAccountDTO, HttpStatus.CREATED);
    }

    /**
     * Retrieves all bank accounts.
     *
     * @return ResponseEntity with the list of bank accounts and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<BankAccountDTO>> getAllAccounts() {
        List<BankAccountDTO> accountDTOs = bankAccountService.getAllAccounts();
        return ResponseEntity.ok(accountDTOs);
    }

    /**
     * Retrieves a bank account by its ID.
     *
     * @param accountId the ID of the bank account.
     * @return ResponseEntity with the account if found, or HTTP status 404 (NOT FOUND) if not found.
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable Integer accountId) {
        return bankAccountService.getAccountById(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deposits an amount into a bank account.
     *
     * @param accountId the ID of the bank account.
     * @param amount    the amount to deposit.
     * @return ResponseEntity with the updated account and HTTP status 200 (OK).
     */
    @PutMapping("/{accountId}/deposit")
    public ResponseEntity<BankAccountDTO> deposit(@PathVariable Integer accountId, @RequestParam BigDecimal amount) {
        try {
            BankAccount updatedAccount = transactionService.deposit(accountId, amount);
            return ResponseEntity.ok(BankAccountMapper.toDTO(updatedAccount));
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
    public ResponseEntity<BankAccountDTO> withdraw(@PathVariable Integer accountId, @RequestParam BigDecimal amount) {
        try {
            BankAccount updatedAccount = transactionService.withdraw(accountId, amount);
            return ResponseEntity.ok(BankAccountMapper.toDTO(updatedAccount));
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
        Optional<BankAccountDTO> deletedAccount = bankAccountService.deleteAccountById(accountId);
        return deletedAccount
                .map(accountDTO -> ResponseEntity.ok(accountDTO))
                .orElse(ResponseEntity.notFound().build());
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
        List<BankAccountDTO> accounts = bankAccountService.getAccountsByCustomerId(customerId);
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
            BankAccountDTO updatedAccountDTO = bankAccountService.getAccountById(accountId).orElseThrow();
            return ResponseEntity.ok(updatedAccountDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

