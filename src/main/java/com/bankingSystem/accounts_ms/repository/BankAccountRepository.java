package com.bankingSystem.accounts_ms.repository;

import com.bankingSystem.accounts_ms.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    boolean existsByCustomerId(Integer customerId);

    List<BankAccount> findByCustomerId(Integer customerId);
}
