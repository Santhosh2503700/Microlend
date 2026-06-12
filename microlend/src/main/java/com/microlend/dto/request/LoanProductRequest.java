package com.microlend.dto.request;

import com.microlend.enums.InterestType;
import com.microlend.enums.LoanProductCategory;
import com.microlend.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanProductRequest {
    @NotBlank private String productName;
    @NotNull private LoanProductCategory category;
    @NotNull private BigDecimal minAmount;
    @NotNull private BigDecimal maxAmount;
    @NotNull private Integer tenureMonths;
    @NotNull private BigDecimal interestRatePercent;
    @NotNull private InterestType interestType;
    private BigDecimal processingFeePercent;
    private ProductStatus status;
}
