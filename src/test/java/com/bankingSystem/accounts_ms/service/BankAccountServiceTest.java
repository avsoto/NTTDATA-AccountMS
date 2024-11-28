package com.bankingSystem.accounts_ms.service;

import com.bankingSystem.accounts_ms.dto.BankAccountDTO;
import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.AccountType;
import com.bankingSystem.accounts_ms.model.BankAccount;
import com.bankingSystem.accounts_ms.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private CustomerIntegrationService customerIntegrationService;

    @InjectMocks
    private BankAccountService bankAccountService;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return the saved account when the customer is valid")
    void createAccount_ReturnsSavedAccount_WhenCustomerIsValid() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setCustomerId(1);
        account.setBalance(BigDecimal.valueOf(1000));

        when(bankAccountRepository.save(account)).thenReturn(account);

        when(customerIntegrationService.isCustomerValid(account)).thenReturn(true);  // Pasa un objeto BankAccount

        String url = "http://localhost:8080/customers/" + account.getCustomerId();
        when(restTemplate.getForEntity(eq(url), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));

        // Act
        BankAccount result = bankAccountService.createAccount(account);

        // Assert
        assertNotNull(result);
        assertEquals(account, result);
        verify(bankAccountRepository, times(1)).save(account);
    }




    @Test
    @DisplayName("Should throw an exception when the customer is invalid")
    void createAccount_ThrowsException_WhenCustomerIsInvalid() {
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
    @DisplayName("Should throw an exception when the customer microservice fails")
    void createAccount_ThrowsException_OnCustomerMicroserviceFailure() {
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

        assertEquals("Customer not found for ID: " + customerId, exception.getMessage());
    }


    @Test
    @DisplayName("Should return all accounts")
    void getAllAccounts_ReturnsAllAccounts() {
        // Arrange
        List<BankAccountDTO> accounts = List.of(
                new BankAccountDTO(1, "1", BigDecimal.valueOf(1000), "SAVINGS", 1),
                new BankAccountDTO(2, "2", BigDecimal.valueOf(1500), "CHECKING", 2)
        );
        when(bankAccountRepository.findAll()).thenReturn(List.of(
                new BankAccount(1, "1", BigDecimal.valueOf(1000), AccountType.SAVINGS, 1),
                new BankAccount(2, "2", BigDecimal.valueOf(1500), AccountType.CHECKING, 2)
        ));

        // Act
        List<BankAccountDTO> result = bankAccountService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bankAccountRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return the account when it exists")
    void getAccountById_ReturnsAccount_WhenExists() {
        // Arrange
        BankAccountDTO accountDTO = new BankAccountDTO(1, "1", BigDecimal.valueOf(1000), "SAVINGS", 1);
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(new BankAccount(1, "1", BigDecimal.valueOf(1000), AccountType.SAVINGS, 1)));

        // Act
        Optional<BankAccountDTO> result = bankAccountService.getAccountById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(accountDTO, result.get());
        verify(bankAccountRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty when the account does not exist")
    void getAccountById_ReturnsEmpty_WhenNotExists() {
        // Arrange
        when(bankAccountRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        Optional<BankAccountDTO> result = bankAccountService.getAccountById(1);

        // Assert
        assertTrue(result.isEmpty());
        verify(bankAccountRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should increase the balance when the deposit amount is valid")
    void deposit_IncreasesBalance_WhenAmountIsValid() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1);
        account.setBalance(BigDecimal.valueOf(1000));
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any())).thenReturn(account);

        // Act
        BankAccount result = transactionService.deposit(1, BigDecimal.valueOf(500));

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1500), result.getBalance());
        verify(bankAccountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("Should throw an exception when the deposit amount is negative")
    void deposit_ThrowsException_WhenAmountIsNegative() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> transactionService.deposit(1, BigDecimal.valueOf(-100)));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw an exception when the account is not found")
    void deposit_ThrowsException_WhenAccountIsNotFound() {
        // Arrange
        Integer accountId = 999; // ID that doesn't exist
        BigDecimal depositAmount = new BigDecimal("100.00");

        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> transactionService.deposit(accountId, depositAmount));

        assertEquals("Account not found for ID: " + accountId, exception.getMessage());
    }


    @Test
    @DisplayName("Should decrease the balance when the withdrawal amount is valid")
    void withdraw_ShouldDecreaseBalance_WhenValidAmount() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(1000));
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any())).thenReturn(account);

        // Act
        BankAccount result = transactionService.withdraw(1, BigDecimal.valueOf(500));

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500), result.getBalance());
        verify(bankAccountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("Should throw an exception when there is insufficient balance in a savings account")
    void withdraw_ThrowsException_WhenInsufficientBalanceInSavingsAccount() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(100));
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(BusinessException.class, () -> transactionService.withdraw(1, BigDecimal.valueOf(200)));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw an exception when the withdrawal amount is zero or negative")
    void withdraw_ThrowsException_WhenAmountIsZeroOrNegative() {
        // Arrange
        Integer accountId = 1;
        BigDecimal invalidAmount = BigDecimal.ZERO;

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> transactionService.withdraw(accountId, invalidAmount));

        // Assert
        assertEquals("Withdrawal amount must be greater than zero.", exception.getMessage());
        verify(bankAccountRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw an exception when the withdrawal exceeds the overdraft limit for a checking account")
    void validateWithdrawal_ThrowsException_WhenExceedsOverdraftLimitForCheckingAccount() {
        // Arrange
        BankAccount account = BankAccount.builder()
                .id(1)
                .accountNumber("1")
                .balance(BigDecimal.valueOf(-400))
                .accountType(AccountType.CHECKING)
                .customerId(1)
                .build();

        BigDecimal withdrawAmount = new BigDecimal("200"); // Exceds the limit of -500

        // Repository mock
        when(bankAccountRepository.findById(1))
                .thenReturn(Optional.of(account));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> {
                    transactionService.withdraw(account.getId(), withdrawAmount);
                });

        // Assert
        assertEquals("Withdrawal exceeds overdraft limit for checking account.", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete the account when it exists")
    void deleteAccountById_DeletesAccount_WhenExists() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1);
        when(bankAccountRepository.findById(1)).thenReturn(Optional.of(account));

        // Act
        Optional<BankAccountDTO> result = bankAccountService.deleteAccountById(1);

        // Assert
        assertTrue(result.isPresent());
        verify(bankAccountRepository, times(1)).delete(account);
    }

    @Test
    @DisplayName("Should throw an exception when the account does not exist")
    void deleteAccountById_ThrowsException_WhenNotExists() {
        // Arrange
        when(bankAccountRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> bankAccountService.deleteAccountById(1));
        verify(bankAccountRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw an exception when deleting the account fails")
    void deleteAccountById_ThrowsException_WhenDeleteFails() {
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
    @DisplayName("Should return accounts when the customer exists")
    void getAccountsByCustomerId_ReturnsAccounts_WhenCustomerExists() {
        // Arrange
        List<BankAccountDTO> accounts = List.of(
                new BankAccountDTO(1, "1", BigDecimal.valueOf(1000), "SAVINGS", 1)
        );
        when(bankAccountRepository.findByCustomerId(1)).thenReturn(List.of(
                new BankAccount(1, "1", BigDecimal.valueOf(1000), AccountType.SAVINGS, 1)
        ));

        // Act
        List<BankAccountDTO> result = bankAccountService.getAccountsByCustomerId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bankAccountRepository, times(1)).findByCustomerId(1);
    }

    @Test
    @DisplayName("Should return true when the account exists for the customer")
    void accountExists_ReturnsTrue_WhenExistsForCustomer() {
        // Arrange
        when(bankAccountRepository.existsByCustomerId(1)).thenReturn(true);

        // Act
        boolean result = bankAccountService.accountExists(1);

        // Assert
        assertTrue(result);
        verify(bankAccountRepository, times(1)).existsByCustomerId(1);
    }

    @Test
    @DisplayName("Should return false when no account exists for the customer")
    void accountExists_ReturnsFalse_WhenNoAccountExistsForCustomer() {
        // Arrange
        when(bankAccountRepository.existsByCustomerId(1)).thenReturn(false);

        // Act
        boolean result = bankAccountService.accountExists(1);

        // Assert
        assertFalse(result);
        verify(bankAccountRepository, times(1)).existsByCustomerId(1);
    }

    @Test
    @DisplayName("Should return true when the account exists")
    void updateBalance_ReturnsTrue_WhenAccountExists() {
        // Arrange
        BankAccount account = new BankAccount();
        account.setId(1);
        account.setBalance(BigDecimal.valueOf(1000));
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
    @DisplayName("Should return false when the account does not exist")
    void updateBalance_ReturnsFalse_WhenAccountDoesNotExist() {
        // Arrange
        when(bankAccountRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        boolean result = bankAccountService.updateBalance(1, BigDecimal.valueOf(2000));

        // Assert
        assertFalse(result);
        verify(bankAccountRepository, never()).save(any());
    }
}