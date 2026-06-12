package com.microlend.service;

import com.microlend.dto.request.BorrowerKYCRequest;
import com.microlend.entity.BorrowerKYC;
import com.microlend.enums.KYCStatus;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.BorrowerKYCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowerKYCService {

    private final BorrowerKYCRepository kycRepository;

    public BorrowerKYC create(BorrowerKYCRequest req) {
        BorrowerKYC kyc = BorrowerKYC.builder()
                .borrowerID(req.getBorrowerID())
                .documentType(req.getDocumentType())
                .documentRef(req.getDocumentRef())
                .verifiedByID(req.getVerifiedByID())
                .verificationDate(req.getVerificationDate())
                .status(req.getStatus() != null ? req.getStatus() : KYCStatus.PENDING)
                .build();
        return kycRepository.save(kyc);
    }

    public List<BorrowerKYC> getByBorrower(Long borrowerID) {
        return kycRepository.findByBorrowerID(borrowerID);
    }

    public BorrowerKYC getById(Long id) {
        return kycRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC record not found: " + id));
    }

    public BorrowerKYC updateStatus(Long id, KYCStatus status, Long verifiedByID) {
        BorrowerKYC kyc = getById(id);
        kyc.setStatus(status);
        kyc.setVerifiedByID(verifiedByID);
        return kycRepository.save(kyc);
    }

    public List<BorrowerKYC> getAll() {
        return kycRepository.findAll();
    }
}
