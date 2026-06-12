package com.microlend.entity;

import com.microlend.enums.InterestType;
import com.microlend.enums.LoanProductCategory;
import com.microlend.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "loan_products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productID;

    @Column(nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    private LoanProductCategory category;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer tenureMonths;
    private BigDecimal interestRatePercent;

    @Enumerated(EnumType.STRING)
    private InterestType interestType;

    private BigDecimal processingFeePercent;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
}
