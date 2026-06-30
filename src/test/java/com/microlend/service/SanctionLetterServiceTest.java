package com.microlend.service;

import com.microlend.dto.request.SanctionLetterRequest;
import com.microlend.entity.LoanApplication;
import com.microlend.entity.SanctionLetter;
import com.microlend.enums.ApplicationStatus;
import com.microlend.enums.SanctionStatus;
import com.microlend.exception.BadRequestException;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.LoanApplicationRepository;
import com.microlend.repository.SanctionLetterRepository;
import com.microlend.service.impl.SanctionLetterServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SanctionLetterService Tests")
class SanctionLetterServiceTest {

    @Mock private SanctionLetterRepository sanctionRepository;
    @Mock private LoanApplicationRepository applicationRepository;
    @InjectMocks private SanctionLetterServiceImpl sanctionLetterService;

    private SanctionLetterRequest validRequest;
    private LoanApplication approvedApp;
    private SanctionLetter issuedLetter;

    @BeforeEach
    void setUp() {
        validRequest = new SanctionLetterRequest();
        validRequest.setApplicationID(1L);
        validRequest.setSanctionedAmount(new BigDecimal("25000"));
        validRequest.setInterestRate(new BigDecimal("12.00"));
        validRequest.setTenure(12);
        validRequest.setEmiAmount(new BigDecimal("2219.92"));
        validRequest.setDisbursalConditions("Borrower must attend centre meeting.");

        approvedApp = LoanApplication.builder()
                .applicationID(1L)
                .borrowerID(1L)
                .status(ApplicationStatus.APPROVED)
                .build();

        issuedLetter = SanctionLetter.builder()
                .sanctionID(1L)
                .applicationID(1L)
                .sanctionedAmount(new BigDecimal("25000"))
                .interestRate(new BigDecimal("12.00"))
                .tenure(12)
                .emiAmount(new BigDecimal("2219.92"))
                .acceptedByBorrower(false)
                .status(SanctionStatus.ISSUED)
                .build();
    }

    @Test
    @DisplayName("issue() - should create ISSUED sanction letter for APPROVED application")
    void issue_approvedApp_success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(approvedApp));
        when(sanctionRepository.save(any())).thenReturn(issuedLetter);

        SanctionLetter result = sanctionLetterService.issue(validRequest);

        assertThat(result.getStatus()).isEqualTo(SanctionStatus.ISSUED);
        assertThat(result.getAcceptedByBorrower()).isFalse();
        assertThat(result.getSanctionedAmount()).isEqualByComparingTo("25000");
    }

    @Test
    @DisplayName("issue() - should throw BadRequestException for non-APPROVED application")
    void issue_notApprovedApp_throwsBadRequest() {
        approvedApp.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(approvedApp));

        assertThatThrownBy(() -> sanctionLetterService.issue(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    @DisplayName("issue() - should throw ResourceNotFoundException when application not found")
    void issue_applicationNotFound_throwsNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());
        validRequest.setApplicationID(99L);

        assertThatThrownBy(() -> sanctionLetterService.issue(validRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("accept() - ISSUED sanction becomes ACCEPTED with acceptedByBorrower=true")
    void accept_issuedLetter_becomesAccepted() {
        when(sanctionRepository.findById(1L)).thenReturn(Optional.of(issuedLetter));
        when(sanctionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SanctionLetter result = sanctionLetterService.accept(1L);

        assertThat(result.getStatus()).isEqualTo(SanctionStatus.ACCEPTED);
        assertThat(result.getAcceptedByBorrower()).isTrue();
    }

    @Test
    @DisplayName("accept() - should throw BadRequestException if already ACCEPTED")
    void accept_alreadyAccepted_throwsBadRequest() {
        issuedLetter.setStatus(SanctionStatus.ACCEPTED);
        when(sanctionRepository.findById(1L)).thenReturn(Optional.of(issuedLetter));

        assertThatThrownBy(() -> sanctionLetterService.accept(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ISSUED");
    }

    @Test
    @DisplayName("accept() - should throw ResourceNotFoundException when sanction not found")
    void accept_notFound_throwsException() {
        when(sanctionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sanctionLetterService.accept(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getAll() - should return all sanction letters")
    void getAll_returnsList() {
        when(sanctionRepository.findAll()).thenReturn(List.of(issuedLetter));

        List<SanctionLetter> result = sanctionLetterService.getAll();

        assertThat(result).hasSize(1);
    }
}
