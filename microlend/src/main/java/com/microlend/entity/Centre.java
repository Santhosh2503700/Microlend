package com.microlend.entity;

import com.microlend.enums.CentreStatus;
import com.microlend.enums.MeetingDay;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "centres")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Centre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long centreID;

    @Column(nullable = false)
    private String centreName;

    private Long branchID;
    private Long fieldOfficerID;
    private String village;

    @Enumerated(EnumType.STRING)
    private MeetingDay meetingDay;

    private LocalTime meetingTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CentreStatus status = CentreStatus.ACTIVE;
}
