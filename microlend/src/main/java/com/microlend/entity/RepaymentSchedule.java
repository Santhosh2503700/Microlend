package com.microlend.entity;

import com.microlend.enums.InstallmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RepaymentSchedule
 *
 * BUG FIX #4: Added amountPaid and paidDate fields.
 * These are required by the fixed CollectionService state machine to track
 * cumulative partial payments against each installment row.
 */
@Entity
@Table(name = "repayment_schedules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleID;

    @Column(nullable = false)
    private Long loanAccountID;

    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal totalDue;

    /** BUG FIX #4: Tracks cumulative amount paid toward this installment. */
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    /** BUG FIX #4: Stamped when installment reaches PAID status. */
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InstallmentStatus status = InstallmentStatus.PENDING;
}
