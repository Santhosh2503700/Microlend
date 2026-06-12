package com.microlend.entity;

import com.microlend.enums.SanctionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sanction_letters")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanctionLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sanctionID;

    @Column(nullable = false, unique = true)
    private Long applicationID;

    private BigDecimal sanctionedAmount;
    private BigDecimal interestRate;
    private Integer tenure;
    private BigDecimal emiAmount;

    @Column(length = 1000)
    private String disbursalConditions;

    @Builder.Default
    private LocalDate issuedDate = LocalDate.now();

    @Builder.Default
    private Boolean acceptedByBorrower = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SanctionStatus status = SanctionStatus.ISSUED;
}
