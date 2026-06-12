package com.microlend.repository;

import com.microlend.entity.RepaymentSchedule;
import com.microlend.enums.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {

    List<RepaymentSchedule> findByLoanAccountID(Long loanAccountID);

    List<RepaymentSchedule> findByLoanAccountIDAndStatus(Long loanAccountID,
                                                          InstallmentStatus status);

    List<RepaymentSchedule> findByStatus(InstallmentStatus status);

    /**
     * BUG FIX #5: Query used by the nightly delinquency engine.
     * Returns all installments that are past their due date and not yet fully paid.
     * Matches PENDING and PARTIAL statuses (PARTIAL added in Bug Fix #4).
     */
    @Query("SELECT r FROM RepaymentSchedule r WHERE r.dueDate < :today " +
           "AND r.status IN ('PENDING', 'PARTIAL')")
    List<RepaymentSchedule> findOverdueInstallments(@Param("today") LocalDate today);
}
