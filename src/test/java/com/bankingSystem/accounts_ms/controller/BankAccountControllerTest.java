package com.bankingSystem.accounts_ms.controller;

import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.BankAccount;
import com.bankingSystem.accounts_ms.service.BankAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BankAccountControllerTest {

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BankAccountController bankAccountController;

    private BankAccount account;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        account = BankAccount.builder()
                .id(1)
                .balance(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    @DisplayName("Should return the saved account when the customer is valid and the account is successfully created")
    public void createAccount_ShouldReturnSavedAccount_WhenCustomerIsValid() {
        when(bankAccountService.createAccount(any(BankAccount.class))).thenReturn(account);

        ResponseEntity<BankAccount> response = bankAccountController.createAccount(account);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(account, response.getBody());
    }

    @Test
    @DisplayName("Should return all accounts when accounts exist and the list is not empty")
    public void getAllAccounts_ShouldReturnAllAccounts_WhenAccountsExist() {
        when(bankAccountService.getAllAccounts()).thenReturn(List.of(account));

        ResponseEntity<?> response = bankAccountController.getAllAccounts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(((List<?>) Objects.requireNonNull(response.getBody())).isEmpty());
    }

    @Test
    @DisplayName("Should return the account when the account ID exists and is found")
    public void getAccountById_ShouldReturnAccount_WhenAccountExists() {
        when(bankAccountService.getAccountById(1)).thenReturn(Optional.of(account));

        ResponseEntity<?> response = bankAccountController.getAccountById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(account, response.getBody());
    }

    @Test
    @DisplayName("Should return true when the customer exists and has accounts")
    public void getAccountsByCustomerId_ShouldReturnTrue_WhenCustomerExists() {
        when(bankAccountService.accountExists(1)).thenReturn(true);

        boolean result = bankAccountController.getAccountByCustomerId(1);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should throw an exception when the customer with the given ID is not found")
    public void getAccountsByCustomerId_ShouldThrowException_WhenCustomerNotFound() {
        when(bankAccountService.getAccountById(2)).thenReturn(Optional.empty());

        ResponseEntity<?> response = bankAccountController.getAccountById(2);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return false when no accounts exist for the given customer")
    public void getAccountsByCustomerId_ShouldReturnFalse_WhenCustomerDoesNotExist() {
        when(bankAccountService.accountExists(1)).thenReturn(false);

        boolean result = bankAccountController.getAccountByCustomerId(1);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should increase the balance when a valid deposit is made")
    public void deposit_ShouldIncreaseBalance_WhenValidAmount() throws BusinessException {
        when(bankAccountService.deposit(1, BigDecimal.valueOf(500))).thenReturn(account);

        ResponseEntity<BankAccount> response = bankAccountController.deposit(1, BigDecimal.valueOf(500));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(account, response.getBody());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when deposit fails due to insufficient funds")
    public void deposit_ShouldReturnBadRequest_WhenInsufficientFunds() throws BusinessException {
        when(bankAccountService.deposit(1, BigDecimal.valueOf(500))).thenThrow(new BusinessException("Insufficient funds"));

        ResponseEntity<BankAccount> response = bankAccountController.deposit(1, BigDecimal.valueOf(500));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should decrease the balance when a valid amount is withdrawn")
    public void withdraw_ShouldDecreaseBalance_WhenValidAmount() throws BusinessException {
        when(bankAccountService.withdraw(1, BigDecimal.valueOf(200))).thenReturn(account);

        ResponseEntity<BankAccount> response = bankAccountController.withdraw(1, BigDecimal.valueOf(200));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(account, response.getBody());
    }

    @Test
    @DisplayName("Should throw an exception when there is insufficient balance")
    public void withdraw_ShouldThrowException_WhenInsufficientBalance() throws BusinessException {
        when(bankAccountService.withdraw(1, BigDecimal.valueOf(2000))).thenThrow(new BusinessException("Insufficient funds"));

        ResponseEntity<BankAccount> response = bankAccountController.withdraw(1, BigDecimal.valueOf(2000));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should delete the account when it exists")
    public void deleteAccountById_ShouldDeleteAccount_WhenAccountExists() {
        when(bankAccountService.deleteAccountById(1)).thenReturn(Optional.of(account));

        ResponseEntity<?> response = bankAccountController.deleteAccountById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should throw an exception when the account does not exist")
    public void deleteAccountById_ShouldThrowException_WhenAccountDoesNotExist() {
        when(bankAccountService.deleteAccountById(1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = bankAccountController.deleteAccountById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return true when the balance is updated successfully and the account exists")
    public void updateBalance_ShouldReturnTrue_WhenAccountExists_AndBalanceIsUpdatedSuccessfully() {
        when(bankAccountService.updateBalance(1, BigDecimal.valueOf(1500))).thenReturn(true);

        ResponseEntity<?> response = bankAccountController.updateBalance(1, Map.of("balance", BigDecimal.valueOf(1500)));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return NOT_FOUND when trying to update balance of a non-existent account")
    public void updateBalance_ShouldReturnFalse_WhenAccountDoesNotExist() {
        when(bankAccountService.updateBalance(1, BigDecimal.valueOf(1500))).thenReturn(false);

        ResponseEntity<?> response = bankAccountController.updateBalance(1, Map.of("balance", BigDecimal.valueOf(1500)));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return true when the customer has one or more active accounts")
    public void hasActiveAccounts_ShouldReturnTrue_WhenCustomerHasActiveAccounts() {
        when(bankAccountService.getAccountsByCustomerId(1)).thenReturn(List.of(account));

        ResponseEntity<Boolean> response = bankAccountController.hasActiveAccounts(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    @DisplayName("Should return false when the customer has zero active accounts")
    public void hasActiveAccounts_ShouldReturnFalse_WhenCustomerHasNoActiveAccounts() {
        when(bankAccountService.getAccountsByCustomerId(1)).thenReturn(List.of());

        ResponseEntity<Boolean> response = bankAccountController.hasActiveAccounts(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
    }

    @Test
    @DisplayName("Should return INTERNAL_SERVER_ERROR when an error occurs during the active accounts check")
    public void hasActiveAccounts_ShouldThrowException_WhenErrorOccursDuringCheck() {
        when(bankAccountService.getAccountsByCustomerId(1)).thenReturn(null);

        ResponseEntity<Boolean> response = bankAccountController.hasActiveAccounts(1);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody());
    }
}
