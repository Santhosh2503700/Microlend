package com.microlend.dto.request;

import com.microlend.enums.GroupStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowerGroupRequest {
    @NotBlank private String groupName;
    private Long centreID;
    private Long fieldOfficerID;
    private LocalDate formationDate;
    private Integer memberCount;
    private Boolean jointLiabilityEnabled;
    private GroupStatus status;
}
