package com.microlend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SanctionLetterRequest {
    @NotNull private Long applicationID;
    @NotNull private BigDecimal sanctionedAmount;
    @NotNull private BigDecimal interestRate;
    @NotNull private Integer tenure;
    @NotNull private BigDecimal emiAmount;
    private String disbursalConditions;
}
