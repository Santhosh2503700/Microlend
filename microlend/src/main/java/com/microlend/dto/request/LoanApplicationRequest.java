package com.microlend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {
    @NotNull private Long borrowerID;
    private Long groupID;
    @NotNull private Long loanProductID;
    @NotNull private BigDecimal requestedAmount;
    private String purpose;
    private Long creditOfficerID;
}
