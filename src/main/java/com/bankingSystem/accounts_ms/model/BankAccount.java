package com.bankingSystem.accounts_ms.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Builder
@Table(name = "bankaccount")
@AllArgsConstructor
@NoArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(unique = true, nullable = false, name="account_number")
    private String accountNumber = UUID.randomUUID().toString();

    @Column(nullable = false, name="balance")
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="account_type")
    private AccountType accountType;

    @Column(nullable = false, name="customer_id")
    private Integer customerId;
}
