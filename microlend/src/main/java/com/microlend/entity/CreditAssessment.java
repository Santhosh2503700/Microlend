package com.microlend.entity;

import com.microlend.enums.CreditRecommendation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "credit_assessments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreditAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assessmentID;

    @Column(nullable = false)
    private Long borrowerID;

    private Long assessedByID;
    private LocalDate assessmentDate;
    private Integer internalCreditScore;
    private BigDecimal debtBurdenRatio;

    @Enumerated(EnumType.STRING)
    private CreditRecommendation recommendation;

    @Column(length = 1000)
    private String remarks;
}
