package com.microlend.dto.request;

import com.microlend.enums.MeetingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CentreMeetingRequest {
    @NotNull private Long centreID;
    private LocalDate meetingDate;
    private Long conductedByID;
    private Integer attendanceCount;
    private BigDecimal collectionAmount;
    private MeetingStatus status;
}
