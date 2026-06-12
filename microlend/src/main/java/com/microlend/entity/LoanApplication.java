package com.microlend.entity;

import com.microlend.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationID;

    @Column(nullable = false)
    private Long borrowerID;

    private Long groupID;

    @Column(nullable = false)
    private Long loanProductID;

    private BigDecimal requestedAmount;
    private String purpose;

    @Builder.Default
    private LocalDate applicationDate = LocalDate.now();

    private Long creditOfficerID;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.DRAFT;
}
