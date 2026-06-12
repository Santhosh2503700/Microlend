package com.microlend.dto.request;

import com.microlend.enums.DelinquencyAction;
import com.microlend.enums.DelinquencyStatus;
import com.microlend.enums.PARBucket;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DelinquencyCaseRequest {
    @NotNull private Long loanAccountID;
    private Integer dpd;
    private PARBucket parBucket;
    private Long assignedCollectionsOfficerID;
    private DelinquencyAction action;
    private DelinquencyStatus status;
}
