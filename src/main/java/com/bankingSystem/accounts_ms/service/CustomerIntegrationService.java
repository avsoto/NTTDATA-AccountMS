package com.bankingSystem.accounts_ms.service;

import com.bankingSystem.accounts_ms.exceptions.BusinessException;
import com.bankingSystem.accounts_ms.model.BankAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CustomerIntegrationService {

    private final RestTemplate restTemplate;


    @Value("${customer.ms.url}")
    private String customerMicroserviceUrl;

    /**
     * Validates whether the customer exists by calling the customer microservice.
     *
     * @param bankAccount the {@link BankAccount} to check for customer validity.
     * @return true if the customer exists, otherwise false.
     */
    public boolean isCustomerValid(BankAccount bankAccount) {
        return customerExists(bankAccount.getCustomerId());
    }

    /**
     * Calls the customer microservice to check if a customer exists.
     *
     * @param customerId the ID of the customer.
     * @return true if the customer exists, otherwise false.
     * @throws BusinessException if an error occurs while calling the customer microservice.
     */
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


}
