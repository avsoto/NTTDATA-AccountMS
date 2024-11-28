package com.bankingSystem.accounts_ms.dto;

import com.bankingSystem.accounts_ms.model.AccountType;
import com.bankingSystem.accounts_ms.model.BankAccount;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankAccountMapper {

    public static BankAccountDTO toDTO(BankAccount bankAccount) {
        return new BankAccountDTO(
                bankAccount.getId(),
                bankAccount.getAccountNumber(),
                bankAccount.getBalance(),
                bankAccount.getAccountType() != null ? bankAccount.getAccountType().name() : null,
                bankAccount.getCustomerId()
        );
    }

    public static BankAccount fromDTO(BankAccountDTO dto) {
        BankAccount account = new BankAccount();
        account.setId(dto.getId());

        // Generar un UUID si no se proporciona un accountNumber
        account.setAccountNumber(
                dto.getAccountNumber() != null ? dto.getAccountNumber() : UUID.randomUUID().toString()
        );

        account.setBalance(dto.getBalance());
        account.setAccountType(dto.getAccountType() != null ? AccountType.valueOf(dto.getAccountType()) : null);
        account.setCustomerId(dto.getCustomerId());
        return account;
    }


    public static List<BankAccountDTO> toDTOList(List<BankAccount> bankAccounts) {
        return bankAccounts.stream()
                .map(BankAccountMapper::toDTO)
                .collect(Collectors.toList());
    }
}
