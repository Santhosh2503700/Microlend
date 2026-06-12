package com.microlend.dto.request;

import com.microlend.enums.CentreStatus;
import com.microlend.enums.MeetingDay;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalTime;

@Data
public class CentreRequest {
    @NotBlank private String centreName;
    private Long branchID;
    private Long fieldOfficerID;
    private String village;
    private MeetingDay meetingDay;
    private LocalTime meetingTime;
    private CentreStatus status;
}
