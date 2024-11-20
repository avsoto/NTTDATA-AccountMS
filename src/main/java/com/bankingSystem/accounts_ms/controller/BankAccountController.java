package com.bankingSystem.accounts_ms.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public ResponseEntity<?> getAllAccounts() {
        List<BankAccount> accounts = bankAccountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<BankAccount> createAccount(@RequestBody BankAccount bankAccount) {
        BankAccount createdAccount = bankAccountService.createAccount(bankAccount);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable Integer accountId) {
        Optional<BankAccount> account = bankAccountService.getAccountById(accountId);
        return account.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public boolean getAccountByCustomerId(@PathVariable Integer customerId) {
        return bankAccountService.accountExists(customerId);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> deleteAccountById(@PathVariable Integer accountId) {
        Optional<BankAccount> deletedAccount = bankAccountService.deleteAccountById(accountId);
        return deletedAccount
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<Boolean> hasActiveAccounts(@PathVariable Integer customerId) {
        List<BankAccount> accounts = bankAccountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(!accounts.isEmpty());
    }

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

