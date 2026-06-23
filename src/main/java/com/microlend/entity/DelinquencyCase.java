package com.microlend.entity;

import com.microlend.enums.DelinquencyAction;
import com.microlend.enums.DelinquencyStatus;
import com.microlend.enums.PARBucket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name = "delinquency_cases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DelinquencyCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long delinquencyID;

    @Column(nullable = false)
    private Long loanAccountID;

    private Integer dpd; // Days Past Due

    @Enumerated(EnumType.STRING)
    private PARBucket parBucket;

    private Long assignedCollectionsOfficerID;

    @Builder.Default
    private LocalDate openedDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private DelinquencyAction action;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DelinquencyStatus status = DelinquencyStatus.OPEN;
}
