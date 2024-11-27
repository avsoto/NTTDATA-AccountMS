package com.bankingSystem.accounts_ms.service;

import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.AccountType;
import com.bankingSystem.accounts_ms.model.BankAccount;
import com.bankingSystem.accounts_ms.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BankAccountService bankAccountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_ReturnsSavedAccount_WhenCustomerIsValid() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCustomerId(1);
        account.setBalance(BigDecimal.valueOf(1000));
        when(bankAccountRepository.save(account)).thenReturn(account);
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));

        // Act
        BankAccount result = bankAccountService.createAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals(account, result);
        verify(bankAccountRepository, times(1)).save(account);
    }

    @Test
    void createAccount_ShouldThrowException_WhenCustomerIsInvalid() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCustomerId(1);
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(false));

        // Act & Assert
        assertThrows(BusinessException.class, () -> bankAccountService.createAccount(account));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void createAccount_ShouldThrowException_WhenCustomerMicroserviceFails() {
        // Arrange
        Integer customerId = 999; // ID of the client that fails
        BankAccount bankAccount = BankAccount.builder()
                .id(1)
                .accountNumber("12345")
                .balance(BigDecimal.ZERO)
                .accountType(AccountType.CHECKING)
                .customerId(customerId)
                .build();

        String url = "http://localhost:8080/customers" + "/" + customerId;
        when(restTemplate.getForEntity(url, Boolean.class))
                .thenThrow(new RuntimeException("Simulated microservice failure"));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bankAccountService.createAccount(bankAccount));

        assertEquals("Customer with ID " + customerId + " not found.", exception.getMessage());
    }


    @Test
    void getAllAccounts_ShouldReturnAllAccounts() {
        // Arrange
        List<BankAccount> accounts = List.of(
                BankAccount.builder()
                        .id(1)
                        .accountNumber("1")
                        .balance(BigDecimal.valueOf(1000))
                        .accountType(AccountType.SAVINGS)
                        .customerId(1)
                        .build(),

                BankAccount.builder()
                        .id(2)
                        .accountNumber("2")
                        .balance(BigDecimal.valueOf(1500))
                        .accountType(AccountType.CHECKING)
                        .customerId(2)
                        .build()
        );
        when(bankAccountRepository.findAll()).thenReturn(accounts);

        // Act
        List<BankAccount> result = bankAccountService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bankAccountRepository, times(1)).findAll();
    }

    @Test
    void getAccountById_ShouldReturnAccount_WhenAccountExists() {
        // Arrange
        BankAccount account = BankAccount.builder()
                .id(1)
                .accountNumber("1")
                .balance(BigDecimal.valueOf(1000))
                .accountType(AccountType.SAVINGS)
                .customerId(1)
                .build();
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));

        // Act
        Optional<BankAccount> result = bankAccountService.getAccountById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(account, result.get());
        verify(bankAccountRepository, times(1)).findById(1);
    }

    @Test
    void getAccountById_ShouldReturnEmpty_WhenAccountDoesNotExist() {
        // Arrange
        when(bankAccountRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        Optional<BankAccount> result = bankAccountService.getAccountById(1);

        // Assert
        assertTrue(result.isEmpty());
        verify(bankAccountRepository, times(1)).findById(1);
    }

    @Test
    void deposit_ShouldIncreaseBalance_WhenValidAmount() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1);
        account.setBalance(BigDecimal.valueOf(1000));
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any())).thenReturn(account);

        // Act
        BankAccount result = bankAccountService.deposit(1, BigDecimal.valueOf(500));

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1500), result.getBalance());
        verify(bankAccountRepository, times(1)).save(account);
    }

    @Test
    void deposit_ShouldThrowException_WhenAmountIsNegative() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> bankAccountService.deposit(1, BigDecimal.valueOf(-100)));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void deposit_ShouldThrowException_WhenAccountNotFound() {
        // Arrange
        Integer accountId = 999; // ID that doesn't exist
        BigDecimal depositAmount = new BigDecimal("100.00");

        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bankAccountService.deposit(accountId, depositAmount));

        assertEquals("Account not found for ID: " + accountId, exception.getMessage());
    }


    @Test
    void withdraw_ShouldDecreaseBalance_WhenValidAmount() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(1000));
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any())).thenReturn(account);

        // Act
        BankAccount result = bankAccountService.withdraw(1, BigDecimal.valueOf(500));

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500), result.getBalance());
        verify(bankAccountRepository, times(1)).save(account);
    }

    @Test
    void withdraw_ShouldThrowException_WhenInsufficientBalanceInSavingsAccount() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(100));
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(BusinessException.class, () -> bankAccountService.withdraw(1, BigDecimal.valueOf(200)));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void withdraw_ShouldThrowException_WhenAmountIsLessThanOrEqualToZero() {
        // Arrange
        Integer accountId = 1;
        BigDecimal invalidAmount = BigDecimal.ZERO;

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> bankAccountService.withdraw(accountId, invalidAmount));

        // Assert
        assertEquals("Withdrawal amount must be greater than zero.", exception.getMessage());
        verify(bankAccountRepository, never()).findById(any());
    }

    @Test
    void validateWithdrawal_ShouldThrowException_WhenExceedsOverdraftLimitForCheckingAccount() {
        // Arrange
        BankAccount account = BankAccount.builder()
                .id(1)
                .accountNumber("1")
                .balance(BigDecimal.valueOf(-400))
                .accountType(AccountType.CHECKING)
                .customerId(1)
                .build();

        BigDecimal withdrawAmount = new BigDecimal("200"); // Exceds the limit of -500

        // Repositoy mock
        when(bankAccountRepository.findById(1))
                .thenReturn(Optional.of(account));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> {
                    bankAccountService.withdraw(account.getId(), withdrawAmount);
                });

        // Assert
        assertEquals("Withdrawal exceeds overdraft limit for checking account.", exception.getMessage());
    }

    @Test
    void deleteAccountById_ShouldDeleteAccount_WhenAccountExists() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1);
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));

        // Act
        Optional<BankAccount> result = bankAccountService.deleteAccountById(1);

        // Assert
        assertTrue(result.isPresent());
        verify(bankAccountRepository, times(1)).delete(account);
    }

    @Test
    void deleteAccountById_ShouldThrowException_WhenAccountDoesNotExist() {
        // Arrange
        when(bankAccountRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> bankAccountService.deleteAccountById(1));
        verify(bankAccountRepository, never()).delete(any());
    }

    @Test
    void deleteAccountById_ShouldThrowException_WhenDeleteFails() {
        // Arrange
        Integer accountId = 1;
        BankAccount account = BankAccount.builder()
                .id(accountId)
                .accountNumber("12345")
                .balance(BigDecimal.ZERO)
                .accountType(AccountType.CHECKING)
                .customerId(1)
                .build();

        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doThrow(new RuntimeException("Simulated delete error"))
                .when(bankAccountRepository).delete(account);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bankAccountService.deleteAccountById(accountId));

        assertEquals("Error deleting account with ID: " + accountId, exception.getMessage());
    }

    @Test
    void getAccountsByCustomerId_ShouldReturnAccounts_WhenCustomerExists() {
        // Arrange
        List<BankAccount> accounts = List.of(
                BankAccount.builder()
                        .id(1)
                        .accountNumber("1")
                        .balance(BigDecimal.valueOf(1000))
                        .accountType(AccountType.SAVINGS)
                        .customerId(1)
                        .build()
        );
        when(bankAccountRepository.findByCustomerId(1)).thenReturn(accounts);

        // Act
        List<BankAccount> result = bankAccountService.getAccountsByCustomerId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByCustomerId(1);
    }

    @Test
    void accountExists_ShouldReturnTrue_WhenAccountExistsForCustomer() {
        // Arrange
        when(bankAccountRepository.existsByCustomerId(1)).thenReturn(true);

        // Act
        boolean result = bankAccountService.accountExists(1);

        // Assert
        assertTrue(result);
        verify(bankAccountRepository, times(1)).existsByCustomerId(1);
    }

    @Test
    void accountExists_ShouldReturnFalse_WhenNoAccountExistsForCustomer() {
        // Arrange
        when(bankAccountRepository.existsByCustomerId(1)).thenReturn(false);

        // Act
        boolean result = bankAccountService.accountExists(1);

        // Assert
        assertFalse(result);
        verify(bankAccountRepository, times(1)).existsByCustomerId(1);
    }

    @Test
    void updateBalance_ShouldReturnTrue_WhenAccountExists() {
        // Arrange
        BankAccount account = BankAccount.builder()
                .id(1)
                .accountNumber("1")
                .balance(BigDecimal.valueOf(1000))
                .accountType(AccountType.SAVINGS)
                .customerId(1)
                .build();
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any())).thenReturn(account);

        // Act
        boolean result = bankAccountService.updateBalance(1, BigDecimal.valueOf(2000));

        // Assert
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(2000), account.getBalance());
        verify(bankAccountRepository, times(1)).save(account);
    }

    @Test
    void updateBalance_ShouldReturnFalse_WhenAccountDoesNotExist() {
        // Arrange
        when(bankAccountRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        boolean result = bankAccountService.updateBalance(1, BigDecimal.valueOf(2000));

        // Assert
        assertFalse(result);
        verify(bankAccountRepository, never()).save(any());
    }














}