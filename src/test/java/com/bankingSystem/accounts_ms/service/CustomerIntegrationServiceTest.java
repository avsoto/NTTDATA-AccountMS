package com.bankingSystem.accounts_ms.service;

import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.BankAccount;
import com.bankingSystem.accounts_ms.service.CustomerIntegrationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class CustomerIntegrationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CustomerIntegrationService customerIntegrationService;

    @Value("${customer.ms.url}")
    private String customerMicroserviceUrl = "http://localhost:8080/customers";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(customerIntegrationService, "customerMicroserviceUrl", "http://localhost:8080/customers");
    }

    @Test
    void isCustomerValid_ValidCustomer_ReturnsTrue() {
        // Arrange
        BankAccount bankAccount = new BankAccount();
        bankAccount.setCustomerId(1);

        String url = customerMicroserviceUrl + "/1";

        Mockito.when(restTemplate.getForEntity(url, Boolean.class))
                .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        // Act
        boolean result = customerIntegrationService.isCustomerValid(bankAccount);

        // Assert
        Assertions.assertTrue(result);
        Mockito.verify(restTemplate).getForEntity(url, Boolean.class);
    }

    @Test
    void isCustomerValid_InvalidCustomer_ThrowsBusinessException() {
        // Arrange
        BankAccount bankAccount = new BankAccount();
        bankAccount.setCustomerId(2);

        String url = customerMicroserviceUrl + "/2";

        Mockito.when(restTemplate.getForEntity(url, Boolean.class))
                .thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> customerIntegrationService.isCustomerValid(bankAccount));

        Assertions.assertEquals("Customer with ID 2 not found.", exception.getMessage());
        Mockito.verify(restTemplate).getForEntity(url, Boolean.class);
    }

    @Test
    void customerExists_ValidCustomer_ReturnsTrue() {
        // Arrange
        Integer customerId = 3;
        String url = customerMicroserviceUrl + "/" + customerId;

        Mockito.when(restTemplate.getForEntity(url, Boolean.class))
                .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        // Crea la instancia de BankAccount y establece el customerId
        BankAccount bankAccount = new BankAccount();
        bankAccount.setCustomerId(customerId);

        // Act
        boolean result = customerIntegrationService.isCustomerValid(bankAccount);

        // Assert
        Assertions.assertTrue(result);
        Mockito.verify(restTemplate).getForEntity(url, Boolean.class);
    }

    @Test
    void customerExists_ResponseBodyNull_ThrowsBusinessException() {
        // Arrange
        Integer customerId = 5;
        String url = customerMicroserviceUrl + "/" + customerId;

        Mockito.when(restTemplate.getForEntity(url, Boolean.class))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // Crea la instancia de BankAccount y establece el customerId
        BankAccount bankAccount = new BankAccount();
        bankAccount.setCustomerId(customerId);

        // Act & Assert
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> customerIntegrationService.isCustomerValid(bankAccount));

        Assertions.assertEquals("Customer with ID 5 not found.", exception.getMessage());
        Mockito.verify(restTemplate).getForEntity(url, Boolean.class);
    }

    @Test
    void customerExists_Non2xxResponse_ThrowsBusinessException() {
        // Arrange
        Integer customerId = 4;
        String url = customerMicroserviceUrl + "/" + customerId;

        Mockito.when(restTemplate.getForEntity(url, Boolean.class))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        // Crea la instancia de BankAccount y establece el customerId
        BankAccount bankAccount = new BankAccount();
        bankAccount.setCustomerId(customerId);

        // Act & Assert
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> customerIntegrationService.isCustomerValid(bankAccount));

        Assertions.assertEquals("Customer with ID 4 not found.", exception.getMessage());
        Mockito.verify(restTemplate).getForEntity(url, Boolean.class);
    }

}
