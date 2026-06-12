package com.microlend.dto.request;

import com.microlend.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanApplicationStatusRequest {
    @NotNull private ApplicationStatus status;
    private String remarks;
}
