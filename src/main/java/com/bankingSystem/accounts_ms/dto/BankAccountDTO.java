package com.bankingSystem.accounts_ms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for bank account information.
 * <p>
 * This class is used to transfer bank account data between layers of the application. It contains the account's ID,
 * account number, balance, account type, and customer ID. The account number is automatically generated using a UUID.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountDTO {
    /**
     * The unique identifier for the bank account.
     * <p>
     * This ID is used to identify the bank account in the system.
     * </p>
     */
    private Integer id;

    /**
     * The unique account number associated with the bank account.
     * <p>
     * This number is generated automatically using a UUID.
     * </p>
     */
    private String accountNumber;

    /**
     * The current balance of the bank account.
     * <p>
     * The balance is represented as a {@link BigDecimal} to handle monetary values with high precision.
     * </p>
     */
    private BigDecimal balance;

    /**
     * The type of the bank account, such as "Checking", "Savings", etc.
     * <p>
     * This field specifies the nature of the account.
     * </p>
     */
    private String accountType;

    /**
     * The ID of the customer who owns the bank account.
     * <p>
     * This ID links the bank account to a specific customer.
     * </p>
     */
    private Integer customerId;

    /**
     * Builder class for {@link BankAccountDTO}.
     * <p>
     * This builder class is used to construct {@link BankAccountDTO} objects. It provides a default value for the
     * account number, which is generated using a UUID.
     * </p>
     */
    public static class BankAccountDTOBuilder {
        private String accountNumber = UUID.randomUUID().toString();
    }
}
