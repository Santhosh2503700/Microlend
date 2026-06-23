package com.microlend.entity;

import com.microlend.enums.LoanAccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanAccountID;

    private Long applicationID;
    private Long borrowerID;
    private Long productID;
    private BigDecimal disbursedAmount;
    private LocalDate disbursementDate;
    private BigDecimal totalInterest;
    private BigDecimal totalRepayable;
    private BigDecimal outstandingPrincipal;

    @Builder.Default
    private Integer dpd = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoanAccountStatus status = LoanAccountStatus.ACTIVE;
}
