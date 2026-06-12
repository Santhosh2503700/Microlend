package com.microlend.entity;

import com.microlend.enums.LoanAccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * LoanAccount
 *
 * BUG FIX #5: Added dpd (Days Past Due) field. The delinquency scheduler
 * updates this field nightly to reflect the most overdue installment.
 */
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

    /** BUG FIX #5: Days Past Due — updated nightly by DelinquencyScheduler. */
    @Builder.Default
    private Integer dpd = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoanAccountStatus status = LoanAccountStatus.ACTIVE;
}
