package com.microlend.service;

import com.microlend.dto.request.CreditAssessmentRequest;
import com.microlend.entity.CreditAssessment;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.CreditAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditAssessmentService {

    private final CreditAssessmentRepository assessmentRepository;

    public CreditAssessment create(CreditAssessmentRequest req) {
        CreditAssessment assessment = CreditAssessment.builder()
                .borrowerID(req.getBorrowerID())
                .assessedByID(req.getAssessedByID())
                .assessmentDate(req.getAssessmentDate() != null ? req.getAssessmentDate() : LocalDate.now())
                .internalCreditScore(req.getInternalCreditScore())
                .debtBurdenRatio(req.getDebtBurdenRatio())
                .recommendation(req.getRecommendation())
                .remarks(req.getRemarks())
                .build();
        return assessmentRepository.save(assessment);
    }

    public List<CreditAssessment> getAll() {
        return assessmentRepository.findAll();
    }

    public CreditAssessment getById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit assessment not found: " + id));
    }

    public CreditAssessment update(Long id, CreditAssessmentRequest req) {
        CreditAssessment ca = getById(id);
        if (req.getInternalCreditScore() != null) ca.setInternalCreditScore(req.getInternalCreditScore());
        if (req.getDebtBurdenRatio() != null) ca.setDebtBurdenRatio(req.getDebtBurdenRatio());
        if (req.getRecommendation() != null) ca.setRecommendation(req.getRecommendation());
        if (req.getRemarks() != null) ca.setRemarks(req.getRemarks());
        return assessmentRepository.save(ca);
    }
}
