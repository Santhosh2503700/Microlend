package com.microlend.service;

import com.microlend.dto.request.DisbursementRequest;
import com.microlend.entity.LoanAccount;
import com.microlend.entity.LoanProduct;
import com.microlend.entity.RepaymentSchedule;
import com.microlend.enums.ApplicationStatus;
import com.microlend.enums.InstallmentStatus;
import com.microlend.enums.InterestType;
import com.microlend.enums.LoanAccountStatus;
import com.microlend.exception.BadRequestException;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanDisbursementService {

    private final LoanAccountRepository loanAccountRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanApplicationRepository applicationRepository;
    private final LoanProductRepository productRepository;

    @Transactional
    public LoanAccount disburse(DisbursementRequest req) {
        var application = applicationRepository.findById(req.getApplicationID())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + req.getApplicationID()));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED applications can be disbursed");
        }

        LoanProduct product = productRepository.findById(application.getLoanProductID())
                .orElseThrow(() -> new ResourceNotFoundException("Loan product not found"));

        BigDecimal principal = req.getDisbursedAmount();
        BigDecimal rate = product.getInterestRatePercent();
        int tenure = product.getTenureMonths();

        BigDecimal totalInterest;
        BigDecimal emi;

        if (product.getInterestType() == InterestType.FLAT) {
            // Flat interest: Total Interest = P * R * T / 100
            totalInterest = principal.multiply(rate)
                    .multiply(BigDecimal.valueOf(tenure))
                    .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
            emi = principal.add(totalInterest).divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);
        } else {
            // Reducing balance EMI = P * R * (1+R)^N / ((1+R)^N - 1)
            double monthlyRate = rate.doubleValue() / 1200;
            double emiDouble = principal.doubleValue() * monthlyRate
                    * Math.pow(1 + monthlyRate, tenure)
                    / (Math.pow(1 + monthlyRate, tenure) - 1);
            emi = BigDecimal.valueOf(emiDouble).setScale(2, RoundingMode.HALF_UP);
            totalInterest = emi.multiply(BigDecimal.valueOf(tenure)).subtract(principal).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalRepayable = principal.add(totalInterest);

        LoanAccount account = LoanAccount.builder()
                .applicationID(req.getApplicationID())
                .borrowerID(application.getBorrowerID())
                .productID(application.getLoanProductID())
                .disbursedAmount(principal)
                .disbursementDate(req.getDisbursementDate() != null ? req.getDisbursementDate() : LocalDate.now())
                .totalInterest(totalInterest)
                .totalRepayable(totalRepayable)
                .outstandingPrincipal(principal)
                .status(LoanAccountStatus.ACTIVE)
                .build();

        LoanAccount saved = loanAccountRepository.save(account);

        // Mark application as disbursed
        application.setStatus(ApplicationStatus.DISBURSED);
        applicationRepository.save(application);

        // Generate repayment schedule
        generateSchedule(saved, product, principal, emi, tenure);

        return saved;
    }

    private void generateSchedule(LoanAccount account, LoanProduct product,
                                   BigDecimal principal, BigDecimal emi, int tenure) {
        List<RepaymentSchedule> schedules = new ArrayList<>();
        BigDecimal remainingPrincipal = principal;
        double monthlyRate = product.getInterestRatePercent().doubleValue() / 1200;

        for (int i = 1; i <= tenure; i++) {
            BigDecimal interestDue;
            BigDecimal principalDue;

            if (product.getInterestType() == InterestType.FLAT) {
                interestDue = principal.multiply(product.getInterestRatePercent())
                        .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
                principalDue = principal.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);
            } else {
                interestDue = remainingPrincipal
                        .multiply(BigDecimal.valueOf(monthlyRate))
                        .setScale(2, RoundingMode.HALF_UP);
                principalDue = emi.subtract(interestDue);
            }

            // Last installment adjustment for rounding
            if (i == tenure) {
                principalDue = remainingPrincipal;
            }

            remainingPrincipal = remainingPrincipal.subtract(principalDue);

            RepaymentSchedule schedule = RepaymentSchedule.builder()
                    .loanAccountID(account.getLoanAccountID())
                    .installmentNumber(i)
                    .dueDate(account.getDisbursementDate().plusMonths(i))
                    .principalDue(principalDue)
                    .interestDue(interestDue)
                    .totalDue(principalDue.add(interestDue))
                    .status(InstallmentStatus.PENDING)
                    .build();
            schedules.add(schedule);
        }
        scheduleRepository.saveAll(schedules);
    }

    public LoanAccount getById(Long id) {
        return loanAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan account not found: " + id));
    }

    public List<LoanAccount> getAll() {
        return loanAccountRepository.findAll();
    }

    public List<LoanAccount> getByBorrower(Long borrowerID) {
        return loanAccountRepository.findByBorrowerID(borrowerID);
    }

    public List<RepaymentSchedule> getSchedule(Long loanAccountID) {
        return scheduleRepository.findByLoanAccountID(loanAccountID);
    }
}
