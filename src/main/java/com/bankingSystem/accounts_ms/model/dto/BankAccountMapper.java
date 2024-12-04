package com.bankingSystem.accounts_ms.model.dto;

import com.bankingSystem.accounts_ms.model.AccountType;
import com.bankingSystem.accounts_ms.model.BankAccount;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between {@link BankAccount} and {@link BankAccountDTO}.
 * <p>
 * This class provides static methods to map between the entity object {@link BankAccount} and the Data Transfer Object
 * {@link BankAccountDTO}. The methods help in converting between layers of the application, such as from the service
 * layer to the controller layer, or when transferring data over HTTP.
 * </p>
 */
public class BankAccountMapper {

    /**
     * Converts a {@link BankAccount} entity to a {@link BankAccountDTO}.
     * <p>
     * This method maps all the relevant fields of a {@link BankAccount} to a {@link BankAccountDTO}.
     * If the account type is not null, it converts it to its corresponding {@link String} representation.
     * </p>
     *
     * @param bankAccount the {@link BankAccount} entity to be converted.
     * @return a {@link BankAccountDTO} object with the corresponding data.
     */
    public static BankAccountDTO toDTO(BankAccount bankAccount) {
        return new BankAccountDTO(
                bankAccount.getId(),
                bankAccount.getAccountNumber(),
                bankAccount.getBalance(),
                bankAccount.getAccountType() != null ? bankAccount.getAccountType().name() : null,
                bankAccount.getCustomerId()
        );
    }

    /**
     * Converts a {@link BankAccountDTO} to a {@link BankAccount} entity.
     * <p>
     * This method maps the data from a {@link BankAccountDTO} to a new {@link BankAccount} entity.
     * If no account number is provided in the DTO, it generates a new UUID as the account number.
     * </p>
     *
     * @param dto the {@link BankAccountDTO} to be converted.
     * @return a {@link BankAccount} entity with the data from the DTO.
     */
    public static BankAccount fromDTO(BankAccountDTO dto) {
        BankAccount account = new BankAccount();
        account.setId(dto.getId());

        // Generate a UUID if no accountNumber is provided
        account.setAccountNumber(
                dto.getAccountNumber() != null ? dto.getAccountNumber() : UUID.randomUUID().toString()
        );

        account.setBalance(dto.getBalance());
        account.setAccountType(dto.getAccountType() != null ? AccountType.valueOf(dto.getAccountType()) : null);
        account.setCustomerId(dto.getCustomerId());
        return account;
    }

    /**
     * Converts a list of {@link BankAccount} entities to a list of {@link BankAccountDTO}.
     * <p>
     * This method uses a stream to map each {@link BankAccount} in the list to a {@link BankAccountDTO} object.
     * </p>
     *
     * @param bankAccounts the list of {@link BankAccount} entities to be converted.
     * @return a list of {@link BankAccountDTO} objects with the corresponding data.
     */
    public static List<BankAccountDTO> toDTOList(List<BankAccount> bankAccounts) {
        return bankAccounts.stream()
                .map(BankAccountMapper::toDTO)
                .collect(Collectors.toList());
    }
}
