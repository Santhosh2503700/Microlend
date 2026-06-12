package com.microlend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DisbursementRequest {
    @NotNull private Long applicationID;
    @NotNull private BigDecimal disbursedAmount;
    private LocalDate disbursementDate;
}
