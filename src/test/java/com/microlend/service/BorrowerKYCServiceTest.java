package com.microlend.service;

import com.microlend.dto.request.BorrowerKYCRequest;
import com.microlend.entity.BorrowerKYC;
import com.microlend.enums.KYCDocumentType;
import com.microlend.enums.KYCStatus;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.BorrowerKYCRepository;
import com.microlend.service.impl.BorrowerKYCServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowerKYCService Tests — includes Bug Fix #3 boundary verification")
class BorrowerKYCServiceTest {

    @Mock private BorrowerKYCRepository kycRepository;
    @InjectMocks private BorrowerKYCServiceImpl kycService;

    private BorrowerKYCRequest uploadRequest;
    private BorrowerKYC pendingKyc;

    @BeforeEach
    void setUp() {
        uploadRequest = new BorrowerKYCRequest();
        uploadRequest.setBorrowerID(1L);
        uploadRequest.setDocumentType(KYCDocumentType.NATIONAL_ID);
        uploadRequest.setDocumentRef("LAKSHMI-AADHAAR-SCAN-001.jpg");
        uploadRequest.setStatus(KYCStatus.PENDING);

        pendingKyc = BorrowerKYC.builder()
                .kycID(1L)
                .borrowerID(1L)
                .documentType(KYCDocumentType.NATIONAL_ID)
                .documentRef("LAKSHMI-AADHAAR-SCAN-001.jpg")
                .status(KYCStatus.PENDING)
                .build();
    }


    @Test
    @DisplayName("create() - should save KYC record with PENDING status")
    void create_success_pendingStatus() {
        when(kycRepository.save(any(BorrowerKYC.class))).thenReturn(pendingKyc);

        BorrowerKYC result = kycService.create(uploadRequest);

        assertThat(result.getKycID()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(KYCStatus.PENDING);
        assertThat(result.getBorrowerID()).isEqualTo(1L);
        verify(kycRepository).save(any(BorrowerKYC.class));
    }

    @Test
    @DisplayName("create() - should default to PENDING when status not provided")
    void create_defaultsPending() {
        uploadRequest.setStatus(null);
        when(kycRepository.save(any(BorrowerKYC.class))).thenAnswer(inv -> {
            BorrowerKYC k = inv.getArgument(0);
            assertThat(k.getStatus()).isEqualTo(KYCStatus.PENDING);
            return pendingKyc;
        });

        kycService.create(uploadRequest);
    }

    @Test
    @DisplayName("create() - should save UTILITY_BILL document type")
    void create_utilityBill() {
        uploadRequest.setDocumentType(KYCDocumentType.UTILITY_BILL);
        uploadRequest.setDocumentRef("ELECTRICITY-BILL-JAN24.jpg");
        BorrowerKYC billKyc = BorrowerKYC.builder().kycID(2L).borrowerID(1L)
                .documentType(KYCDocumentType.UTILITY_BILL).status(KYCStatus.PENDING).build();
        when(kycRepository.save(any())).thenReturn(billKyc);

        BorrowerKYC result = kycService.create(uploadRequest);

        assertThat(result.getDocumentType()).isEqualTo(KYCDocumentType.UTILITY_BILL);
    }


    @Test
    @DisplayName("BUG FIX #3 — updateStatus() sets KYC to VERIFIED when called (Credit Officer path)")
    void updateStatus_verified_success() {
        
        when(kycRepository.findById(1L)).thenReturn(Optional.of(pendingKyc));
        when(kycRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BorrowerKYC result = kycService.updateStatus(1L, KYCStatus.VERIFIED, 3L);

        assertThat(result.getStatus()).isEqualTo(KYCStatus.VERIFIED);
        assertThat(result.getVerifiedByID()).isEqualTo(3L);
    }

    @Test
    @DisplayName("BUG FIX #3 — updateStatus() sets KYC to REJECTED")
    void updateStatus_rejected_success() {
        when(kycRepository.findById(1L)).thenReturn(Optional.of(pendingKyc));
        when(kycRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BorrowerKYC result = kycService.updateStatus(1L, KYCStatus.REJECTED, 3L);

        assertThat(result.getStatus()).isEqualTo(KYCStatus.REJECTED);
    }

    @Test
    @DisplayName("BUG FIX #3 — updateStatus() throws ResourceNotFoundException when KYC not found")
    void updateStatus_kycNotFound_throwsException() {
        when(kycRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kycService.updateStatus(99L, KYCStatus.VERIFIED, 3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }


    @Test
    @DisplayName("getByBorrower() - should return all KYC records for a borrower")
    void getByBorrower_returnsAll() {
        BorrowerKYC billKyc = BorrowerKYC.builder().kycID(2L).borrowerID(1L)
                .documentType(KYCDocumentType.UTILITY_BILL).build();
        when(kycRepository.findByBorrowerID(1L)).thenReturn(List.of(pendingKyc, billKyc));

        List<BorrowerKYC> result = kycService.getByBorrower(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BorrowerKYC::getDocumentType)
                .contains(KYCDocumentType.NATIONAL_ID, KYCDocumentType.UTILITY_BILL);
    }

    @Test
    @DisplayName("getById() - should return KYC record when found")
    void getById_found() {
        when(kycRepository.findById(1L)).thenReturn(Optional.of(pendingKyc));

        BorrowerKYC result = kycService.getById(1L);

        assertThat(result.getKycID()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById() - should throw when not found")
    void getById_notFound() {
        when(kycRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kycService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
