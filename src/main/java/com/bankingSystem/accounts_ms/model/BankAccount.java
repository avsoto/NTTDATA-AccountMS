package com.bankingSystem.accounts_ms.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity class representing a bank account.
 * <p>
 * This class defines the structure of a bank account in the system, including
 * the account number, balance, account type, and customer ID associated with it.
 * </p>
 */
@Data
@Entity
@Builder
@Table(name = "bankaccount")
@AllArgsConstructor
@NoArgsConstructor
public class BankAccount {

    /**
     * The unique identifier of the bank account.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    /**
     * The unique account number assigned to the bank account.
     * <p>
     * This field is automatically generated as a UUID string.
     * </p>
     */
    @Column(unique = true, nullable = false, name="account_number")
    private String accountNumber = UUID.randomUUID().toString();

    /**
     * The current balance of the bank account.
     * <p>
     * Default value is set to zero (BigDecimal.ZERO).
     * </p>
     */
    @Column(nullable = false, name="balance")
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * The type of the bank account (e.g., savings or checking).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="account_type")
    private AccountType accountType;

    /**
     * The ID of the customer who owns the bank account.
     */
    @Column(nullable = false, name="customer_id")
    private Integer customerId;
}
