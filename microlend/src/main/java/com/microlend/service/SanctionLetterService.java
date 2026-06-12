package com.microlend.service;

import com.microlend.dto.request.SanctionLetterRequest;
import com.microlend.entity.SanctionLetter;
import com.microlend.enums.ApplicationStatus;
import com.microlend.enums.SanctionStatus;
import com.microlend.exception.BadRequestException;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.LoanApplicationRepository;
import com.microlend.repository.SanctionLetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanctionLetterService {

    private final SanctionLetterRepository sanctionRepository;
    private final LoanApplicationRepository applicationRepository;

    public SanctionLetter issue(SanctionLetterRequest req) {
        // Verify application is APPROVED
        var application = applicationRepository.findById(req.getApplicationID())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + req.getApplicationID()));
        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new BadRequestException("Sanction letter can only be issued for APPROVED applications");
        }

        SanctionLetter letter = SanctionLetter.builder()
                .applicationID(req.getApplicationID())
                .sanctionedAmount(req.getSanctionedAmount())
                .interestRate(req.getInterestRate())
                .tenure(req.getTenure())
                .emiAmount(req.getEmiAmount())
                .disbursalConditions(req.getDisbursalConditions())
                .issuedDate(LocalDate.now())
                .acceptedByBorrower(false)
                .status(SanctionStatus.ISSUED)
                .build();
        return sanctionRepository.save(letter);
    }

    public SanctionLetter accept(Long id) {
        SanctionLetter letter = getById(id);
        if (letter.getStatus() != SanctionStatus.ISSUED) {
            throw new BadRequestException("Only ISSUED sanction letters can be accepted");
        }
        letter.setAcceptedByBorrower(true);
        letter.setStatus(SanctionStatus.ACCEPTED);
        return sanctionRepository.save(letter);
    }

    public SanctionLetter getById(Long id) {
        return sanctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sanction letter not found: " + id));
    }

    public SanctionLetter getByApplicationId(Long applicationID) {
        return sanctionRepository.findAll().stream()
                .filter(s -> s.getApplicationID().equals(applicationID))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sanction letter not found for application: " + applicationID));
    }

    public List<SanctionLetter> getAll() {
        return sanctionRepository.findAll();
    }
}
