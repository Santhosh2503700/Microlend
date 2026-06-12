package com.microlend.dto.request;

import com.microlend.enums.CollectionMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CollectionRequest {
    @NotNull private Long loanAccountID;
    private Long scheduleID;
    @NotNull private BigDecimal collectedAmount;
    private LocalDate collectionDate;
    private Long collectedByID;
    @NotNull private CollectionMode mode;
}
