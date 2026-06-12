package com.microlend.service;

import com.microlend.dto.request.LoanApplicationRequest;
import com.microlend.dto.request.LoanApplicationStatusRequest;
import com.microlend.entity.LoanApplication;
import com.microlend.enums.ApplicationStatus;
import com.microlend.exception.BadRequestException;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository applicationRepository;

    public LoanApplication create(LoanApplicationRequest req) {
        LoanApplication application = LoanApplication.builder()
                .borrowerID(req.getBorrowerID())
                .groupID(req.getGroupID())
                .loanProductID(req.getLoanProductID())
                .requestedAmount(req.getRequestedAmount())
                .purpose(req.getPurpose())
                .applicationDate(LocalDate.now())
                .creditOfficerID(req.getCreditOfficerID())
                .status(ApplicationStatus.DRAFT)
                .build();
        return applicationRepository.save(application);
    }

    public List<LoanApplication> getAll() {
        return applicationRepository.findAll();
    }

    public LoanApplication getById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found: " + id));
    }

    public List<LoanApplication> getByBorrower(Long borrowerID) {
        return applicationRepository.findByBorrowerID(borrowerID);
    }

    public List<LoanApplication> getByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public LoanApplication updateStatus(Long id, LoanApplicationStatusRequest req) {
        LoanApplication application = getById(id);
        // Enforce valid status transitions
        validateStatusTransition(application.getStatus(), req.getStatus());
        application.setStatus(req.getStatus());
        return applicationRepository.save(application);
    }

    public LoanApplication submit(Long id) {
        LoanApplication application = getById(id);
        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT applications can be submitted");
        }
        application.setStatus(ApplicationStatus.SUBMITTED);
        return applicationRepository.save(application);
    }

    private void validateStatusTransition(ApplicationStatus current, ApplicationStatus next) {
        boolean valid = switch (current) {
            case DRAFT -> next == ApplicationStatus.SUBMITTED;
            case SUBMITTED -> next == ApplicationStatus.UNDER_REVIEW;
            case UNDER_REVIEW -> next == ApplicationStatus.APPROVED || next == ApplicationStatus.REJECTED;
            case APPROVED -> next == ApplicationStatus.DISBURSED;
            default -> false;
        };
        if (!valid) {
            throw new BadRequestException("Invalid status transition from " + current + " to " + next);
        }
    }
}
