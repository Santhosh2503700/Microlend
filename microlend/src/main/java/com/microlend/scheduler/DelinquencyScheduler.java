package com.microlend.scheduler;

import com.microlend.entity.AuditLog;
import com.microlend.entity.DelinquencyCase;
import com.microlend.entity.LoanAccount;
import com.microlend.entity.RepaymentSchedule;
import com.microlend.enums.DelinquencyStatus;
import com.microlend.enums.InstallmentStatus;
import com.microlend.enums.LoanAccountStatus;
import com.microlend.enums.PARBucket;
import com.microlend.repository.AuditLogRepository;
import com.microlend.repository.DelinquencyCaseRepository;
import com.microlend.repository.LoanAccountRepository;
import com.microlend.repository.RepaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * DelinquencyScheduler — automated nightly delinquency detection.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * BUG FIX #5: Missing Automated Delinquency Engine
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ORIGINAL BUG:
 *   DelinquencyService had only manual CRUD endpoints. There was no
 *   scheduled automation to:
 *     - Scan RepaymentSchedule for past-due installments
 *     - Compute Days Past Due (DPD) per loan account
 *     - Classify accounts into PAR buckets
 *     - Auto-create DelinquencyCase records
 *   Delinquency cases had to be created manually via API calls, which
 *   is operationally impractical for a portfolio of hundreds of loans.
 *
 * FIX:
 *   A @Scheduled cron job runs every night at 00:30 AM. It:
 *   1. Queries RepaymentSchedule for overdue rows
 *      (dueDate < today, status IN {PENDING, PARTIAL}).
 *   2. Marks each row as OVERDUE.
 *   3. Calculates DPD = today - dueDate.
 *   4. Updates LoanAccount.dpd to the maximum DPD found.
 *   5. Marks account as NPA if DPD >= 90.
 *   6. Classifies into PAR bucket: PAR30, PAR60, PAR90, PAR180.
 *   7. Creates a new DelinquencyCase or updates the existing OPEN case.
 *   8. Writes an AuditLog entry per new case (actor: SYSTEM_SCHEDULER).
 *
 * Requires @EnableScheduling on MicroLendApplication — also added (Bug Fix #5).
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DelinquencyScheduler {

    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final DelinquencyCaseRepository delinquencyRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Nightly delinquency engine — runs at 00:30 AM every day.
     * Cron: second | minute | hour | day | month | weekday
     */
    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void runNightlyDelinquencyCheck() {
        LocalDate today = LocalDate.now();
        log.info("[DelinquencyEngine] Starting nightly run for date: {}", today);

        int overdueMarked = 0;
        int casesCreated = 0;
        int casesUpdated = 0;

        // Step 1: Find all installments that are past due and not yet fully paid
        List<RepaymentSchedule> overdueInstallments =
                scheduleRepository.findOverdueInstallments(today);

        log.info("[DelinquencyEngine] Found {} overdue installment(s).",
                overdueInstallments.size());

        for (RepaymentSchedule installment : overdueInstallments) {
            try {
                LoanAccount account = loanAccountRepository
                        .findById(installment.getLoanAccountID())
                        .orElse(null);
                if (account == null) continue;

                // Step 2: Mark installment as OVERDUE
                if (installment.getStatus() != InstallmentStatus.OVERDUE) {
                    installment.setStatus(InstallmentStatus.OVERDUE);
                    scheduleRepository.save(installment);
                    overdueMarked++;
                }

                // Step 3: Calculate DPD
                long daysOverdue = ChronoUnit.DAYS.between(installment.getDueDate(), today);

                // Step 4: Update account DPD (keep maximum)
                if (daysOverdue > (account.getDpd() != null ? account.getDpd() : 0)) {
                    account.setDpd((int) daysOverdue);
                }

                // Step 5: Mark NPA if DPD >= 90
                if (daysOverdue >= 90
                        && account.getStatus() == LoanAccountStatus.ACTIVE) {
                    account.setStatus(LoanAccountStatus.NPA);
                    log.warn("[DelinquencyEngine] Account {} → NPA. DPD: {}",
                            account.getLoanAccountID(), daysOverdue);
                }
                loanAccountRepository.save(account);

                // Step 6: Classify PAR bucket
                PARBucket bucket = classifyPARBucket(daysOverdue);

                // Step 7: Create or update DelinquencyCase
                Optional<DelinquencyCase> existing = delinquencyRepository
                        .findByLoanAccountIDAndStatus(
                                account.getLoanAccountID(), DelinquencyStatus.OPEN);

                if (existing.isPresent()) {
                    DelinquencyCase dc = existing.get();
                    dc.setDpd((int) daysOverdue);
                    dc.setParBucket(bucket);
                    delinquencyRepository.save(dc);
                    casesUpdated++;
                    log.debug("[DelinquencyEngine] Updated case {} | Account {} | DPD {} | {}",
                            dc.getDelinquencyID(), account.getLoanAccountID(), daysOverdue, bucket);
                } else {
                    DelinquencyCase newCase = DelinquencyCase.builder()
                            .loanAccountID(account.getLoanAccountID())
                            .dpd((int) daysOverdue)
                            .parBucket(bucket)
                            .openedDate(today)
                            .status(DelinquencyStatus.OPEN)
                            .build();
                    delinquencyRepository.save(newCase);
                    casesCreated++;

                    log.info("[DelinquencyEngine] New case | Account {} | DPD {} | {}",
                            account.getLoanAccountID(), daysOverdue, bucket);

                    // Step 8: Audit entry
                    AuditLog audit = AuditLog.builder()
                            .userID(0L) // 0 = SYSTEM_SCHEDULER
                            .action("AUTO_CREATE_DELINQUENCY_CASE")
                            .module("DELINQUENCY")
                            .build();
                    auditLogRepository.save(audit);
                }

            } catch (Exception e) {
                log.error("[DelinquencyEngine] Error processing schedule ID {}: {}",
                        installment.getScheduleID(), e.getMessage(), e);
            }
        }

        log.info("[DelinquencyEngine] Nightly run complete. " +
                "OVERDUE marked: {} | Cases created: {} | Cases updated: {}",
                overdueMarked, casesCreated, casesUpdated);
    }

    /**
     * PAR bucket classification by Days Past Due.
     * PAR30  = 30–59 DPD
     * PAR60  = 60–89 DPD
     * PAR90  = 90–179 DPD  (NPA threshold)
     * PAR180 = 180+ DPD
     */
    private PARBucket classifyPARBucket(long daysOverdue) {
        if (daysOverdue >= 180) return PARBucket.PAR180;
        if (daysOverdue >= 90)  return PARBucket.PAR90;
        if (daysOverdue >= 60)  return PARBucket.PAR60;
        return PARBucket.PAR30;
    }

    /** Manual trigger — callable from DelinquencyController (ADMIN only). */
    public void triggerManualRun() {
        log.info("[DelinquencyEngine] Manual trigger initiated.");
        runNightlyDelinquencyCheck();
    }
}
