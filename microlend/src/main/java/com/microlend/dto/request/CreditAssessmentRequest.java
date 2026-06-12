package com.microlend.dto.request;

import com.microlend.enums.CreditRecommendation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreditAssessmentRequest {
    @NotNull private Long borrowerID;
    private Long assessedByID;
    private LocalDate assessmentDate;
    private Integer internalCreditScore;
    private BigDecimal debtBurdenRatio;
    private CreditRecommendation recommendation;
    private String remarks;
}
