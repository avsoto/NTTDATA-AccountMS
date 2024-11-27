package com.bankingSystem.accounts_ms.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for creating a {@link RestTemplate} bean.
 * This class defines a {@link RestTemplate} instance which can be used throughout the application
 * to perform HTTP requests to external services or microservices.
 *
 * The {@link RestTemplate} bean is registered in the Spring application context and can be injected
 * into any component that requires it, such as the {@link com.bankingSystem.accounts_ms.service.BankAccountService}.
 *
 * @see RestTemplate
 * @see com.bankingSystem.accounts_ms.service.BankAccountService
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a new instance of {@link RestTemplate}.
     *
     * This bean will be automatically available for dependency injection in other parts of the application
     * that require it. It can be used to make HTTP requests to external services or APIs.
     *
     * @return a new {@link RestTemplate} instance.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
