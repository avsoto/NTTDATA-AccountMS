package com.bankingSystem.accounts_ms.service;

import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.BankAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service class for interacting with the customer microservice to validate customer existence.
 * <p>
 * This service communicates with the customer microservice to check whether a customer exists by calling
 * the customer service endpoint. It is used in scenarios where the bank account creation process needs to
 * validate the customer before proceeding.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CustomerIntegrationService {

    @Autowired
    private final RestTemplate restTemplate;

    @Value("${customer.ms.url}")
    private String customerMicroserviceUrl;

    /**
     * Validates whether the customer exists by calling the customer microservice.
     * <p>
     * This method checks if the customer associated with the given bank account exists by calling
     * the customer microservice using the {@link #customerExists(Integer)} method.
     * </p>
     *
     * @param bankAccount the {@link BankAccount} to check for customer validity.
     * @return true if the customer exists, otherwise false.
     */
    public boolean isCustomerValid(BankAccount bankAccount) {
        return customerExists(bankAccount.getCustomerId());
    }

    /**
     * Calls the customer microservice to check if a customer exists.
     * <p>
     * This private method sends a GET request to the customer microservice, checking if a customer exists
     * for the given customer ID. If the microservice responds successfully with a true or false, the result is returned.
     * </p>
     *
     * @param customerId the ID of the customer.
     * @return true if the customer exists, otherwise false.
     * @throws BusinessException if an error occurs while calling the customer microservice.
     */
    private boolean customerExists(Integer customerId) {
        String url = "http://localhost:8080/customers/" + customerId + "/exists";

        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);

            // Log the response status and body
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            // Log the exception and propagate a BusinessException
            System.out.println("Error calling customer microservice for ID: " + customerId + e.getMessage());
        }
        throw new BusinessException("Customer with ID " + customerId + " not found.");
    }
}
