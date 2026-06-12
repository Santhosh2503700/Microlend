package com.microlend.entity;

import com.microlend.enums.ReportScope;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "portfolio_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportID;

    @Enumerated(EnumType.STRING)
    private ReportScope scope;

    private Long scopeRefID; // branchID / productID / fieldOfficerID depending on scope

    private Integer activeLoanCount;
    private BigDecimal totalOutstanding;
    private BigDecimal disbursementValue;
    private BigDecimal collectionRate;
    private BigDecimal par30;
    private BigDecimal par90;
    private BigDecimal npaPercent;

    @Builder.Default
    private LocalDate generatedDate = LocalDate.now();
}
