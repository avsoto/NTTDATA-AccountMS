package com.bankingSystem.accounts_ms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountDTO {
    private Integer id;
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
    private Integer customerId;

    public static class BankAccountDTOBuilder {
        private String accountNumber = UUID.randomUUID().toString();
    }
}
