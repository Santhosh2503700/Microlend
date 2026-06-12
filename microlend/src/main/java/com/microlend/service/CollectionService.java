package com.microlend.service;

import com.microlend.dto.request.CollectionRequest;
import com.microlend.entity.CollectionRecord;
import com.microlend.entity.LoanAccount;
import com.microlend.entity.RepaymentSchedule;
import com.microlend.enums.CollectionStatus;
import com.microlend.enums.InstallmentStatus;
import com.microlend.enums.LoanAccountStatus;
import com.microlend.exception.BadRequestException;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.CollectionRecordRepository;
import com.microlend.repository.LoanAccountRepository;
import com.microlend.repository.RepaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CollectionService
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * BUG FIX #4: Incomplete Repayment Ledger State Machine (Partial Payment)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ORIGINAL BUG (in recordCollection):
 *   When cmp < 0 (collected < totalDue), the code set CollectionStatus = PARTIAL
 *   on the CollectionRecord, but it did NOT update the RepaymentSchedule row.
 *   The installment status remained PENDING, meaning:
 *     1. The schedule appeared as if no payment occurred.
 *     2. The delinquency engine would still flag this installment as OVERDUE.
 *     3. There was no trace of partial progress on the installment row itself.
 *
 * The outstanding principal was subtracted (line:
 *   account.setOutstandingPrincipal(...subtract(req.getCollectedAmount()))
 * but the schedule row was never touched for the partial case.
 *
 * FIX — Three-branch state machine:
 *
 *   1. Full or excess payment (collected >= totalDue):
 *      → InstallmentStatus = PAID
 *      → paidDate stamped
 *      → outstandingPrincipal reduced by principalDue
 *      → account closed if fully repaid
 *
 *   2. Partial payment (0 < collected < totalDue):   ← WAS MISSING
 *      → InstallmentStatus = PARTIAL   (NOT left as PENDING)
 *      → amountPaid on the schedule row is incremented (cumulative support)
 *      → outstandingPrincipal reduced proportionally
 *      → DPD is NOT reset (installment not cleared yet)
 *
 *   3. Zero/negative amount:
 *      → Rejected with BadRequestException
 *
 * Note: InstallmentStatus.PARTIAL is added to the InstallmentStatus enum.
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionService {

    private final CollectionRecordRepository collectionRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanAccountRepository loanAccountRepository;

    @Transactional
    public CollectionRecord recordCollection(CollectionRequest req) {
        // 1. Validate loan account
        LoanAccount account = loanAccountRepository.findById(req.getLoanAccountID())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found: " + req.getLoanAccountID()));

        if (account.getStatus() == LoanAccountStatus.CLOSED) {
            throw new BadRequestException("Loan account is already closed");
        }

        // BUG FIX #4 — Guard: reject zero or negative amounts
        if (req.getCollectedAmount() == null
                || req.getCollectedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Collected amount must be greater than zero. Received: "
                    + req.getCollectedAmount());
        }

        CollectionStatus collectionStatus = CollectionStatus.RECEIVED;

        // 2. If a scheduleID is provided, apply the three-branch state machine
        if (req.getScheduleID() != null) {
            RepaymentSchedule schedule = scheduleRepository.findById(req.getScheduleID())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Schedule not found: " + req.getScheduleID()));

            // How much is still owed (supports cumulative partial payments)
            BigDecimal alreadyPaid = schedule.getAmountPaid() != null
                    ? schedule.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal remainingDue = schedule.getTotalDue().subtract(alreadyPaid);

            int cmp = req.getCollectedAmount().compareTo(remainingDue);

            if (cmp >= 0) {
                // ── FULL PAYMENT ──────────────────────────────────────────
                schedule.setStatus(InstallmentStatus.PAID);
                schedule.setAmountPaid(schedule.getTotalDue());
                schedule.setPaidDate(LocalDate.now());
                collectionStatus = cmp > 0
                        ? CollectionStatus.EXCESS
                        : CollectionStatus.RECEIVED;

                log.info("[Collection] Account {} Installment #{} → PAID. Amount: {}",
                        account.getLoanAccountID(), schedule.getInstallmentNumber(),
                        req.getCollectedAmount());

            } else {
                // ── PARTIAL PAYMENT (BUG FIX #4 — was missing) ────────────
                // Previously: installment stayed PENDING, nothing updated.
                // Fixed:      mark PARTIAL, accumulate amountPaid.
                BigDecimal newTotalPaid = alreadyPaid.add(req.getCollectedAmount());
                schedule.setAmountPaid(newTotalPaid);
                schedule.setStatus(InstallmentStatus.PARTIAL);
                collectionStatus = CollectionStatus.PARTIAL;

                log.info("[Collection] Account {} Installment #{} → PARTIAL. " +
                        "Collected: {}, Total paid: {}, Still owed: {}",
                        account.getLoanAccountID(), schedule.getInstallmentNumber(),
                        req.getCollectedAmount(), newTotalPaid,
                        schedule.getTotalDue().subtract(newTotalPaid));
            }

            scheduleRepository.save(schedule);
        }

        // 3. Reduce outstanding principal
        account.setOutstandingPrincipal(
                account.getOutstandingPrincipal()
                        .subtract(req.getCollectedAmount())
                        .max(BigDecimal.ZERO));

        // 4. Close account if fully repaid
        if (account.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) == 0) {
            account.setStatus(LoanAccountStatus.CLOSED);
            log.info("[Collection] Account {} fully repaid → CLOSED.",
                    account.getLoanAccountID());
        }
        loanAccountRepository.save(account);

        // 5. Persist the collection record
        CollectionRecord record = CollectionRecord.builder()
                .loanAccountID(req.getLoanAccountID())
                .scheduleID(req.getScheduleID())
                .collectedAmount(req.getCollectedAmount())
                .collectionDate(req.getCollectionDate() != null
                        ? req.getCollectionDate() : LocalDate.now())
                .collectedByID(req.getCollectedByID())
                .mode(req.getMode())
                .status(collectionStatus)
                .build();

        return collectionRepository.save(record);
    }

    public List<CollectionRecord> getAll() {
        return collectionRepository.findAll();
    }

    public List<CollectionRecord> getByLoanAccount(Long loanAccountID) {
        return collectionRepository.findByLoanAccountID(loanAccountID);
    }

    public CollectionRecord getById(Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Collection record not found: " + id));
    }
}
