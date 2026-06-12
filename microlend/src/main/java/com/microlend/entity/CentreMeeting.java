package com.microlend.entity;

import com.microlend.enums.MeetingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "centre_meetings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CentreMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingID;

    @Column(nullable = false)
    private Long centreID;

    private LocalDate meetingDate;
    private Long conductedByID;

    @Builder.Default
    private Integer attendanceCount = 0;

    @Builder.Default
    private BigDecimal collectionAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;
}
