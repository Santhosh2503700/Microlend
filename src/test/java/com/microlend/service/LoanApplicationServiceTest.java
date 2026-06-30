package com.microlend.service;

import com.microlend.dto.request.LoanApplicationRequest;
import com.microlend.dto.request.LoanApplicationStatusRequest;
import com.microlend.entity.LoanApplication;
import com.microlend.enums.ApplicationStatus;
import com.microlend.exception.BadRequestException;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.LoanApplicationRepository;
import com.microlend.service.impl.LoanApplicationServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanApplicationService Tests")
class LoanApplicationServiceTest {

    @Mock private LoanApplicationRepository applicationRepository;
    @InjectMocks private LoanApplicationServiceImpl loanApplicationService;

    private LoanApplicationRequest validRequest;
    private LoanApplication draftApp;

    @BeforeEach
    void setUp() {
        validRequest = new LoanApplicationRequest();
        validRequest.setBorrowerID(1L);
        validRequest.setGroupID(1L);
        validRequest.setLoanProductID(1L);
        validRequest.setRequestedAmount(new BigDecimal("25000"));
        validRequest.setPurpose("Purchase agricultural inputs");
        validRequest.setCreditOfficerID(3L);

        draftApp = LoanApplication.builder()
                .applicationID(1L)
                .borrowerID(1L)
                .groupID(1L)
                .loanProductID(1L)
                .requestedAmount(new BigDecimal("25000"))
                .purpose("Purchase agricultural inputs")
                .applicationDate(LocalDate.now())
                .status(ApplicationStatus.DRAFT)
                .build();
    }


    @Test
    @DisplayName("create() - should always start with DRAFT status")
    void create_startsAsDraft() {
        when(applicationRepository.save(any(LoanApplication.class))).thenReturn(draftApp);

        LoanApplication result = loanApplicationService.create(validRequest);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.DRAFT);
        assertThat(result.getApplicationID()).isEqualTo(1L);
        verify(applicationRepository).save(any(LoanApplication.class));
    }

    @Test
    @DisplayName("create() - should set applicationDate to today")
    void create_setsApplicationDateToToday() {
        when(applicationRepository.save(any())).thenAnswer(inv -> {
            LoanApplication a = inv.getArgument(0);
            assertThat(a.getApplicationDate()).isEqualTo(LocalDate.now());
            return draftApp;
        });

        loanApplicationService.create(validRequest);
    }


    @Test
    @DisplayName("submit() - DRAFT should transition to SUBMITTED")
    void submit_draftToSubmitted() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplication result = loanApplicationService.submit(1L);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
    }

    @Test
    @DisplayName("submit() - should throw BadRequestException if not DRAFT")
    void submit_notDraft_throwsBadRequest() {
        draftApp.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));

        assertThatThrownBy(() -> loanApplicationService.submit(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DRAFT");
    }


    @Test
    @DisplayName("updateStatus() - SUBMITTED to UNDER_REVIEW is valid")
    void updateStatus_submittedToUnderReview() {
        draftApp.setStatus(ApplicationStatus.SUBMITTED);
        LoanApplicationStatusRequest req = new LoanApplicationStatusRequest();
        req.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplication result = loanApplicationService.updateStatus(1L, req);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("updateStatus() - UNDER_REVIEW to APPROVED is valid")
    void updateStatus_underReviewToApproved() {
        draftApp.setStatus(ApplicationStatus.UNDER_REVIEW);
        LoanApplicationStatusRequest req = new LoanApplicationStatusRequest();
        req.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplication result = loanApplicationService.updateStatus(1L, req);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("updateStatus() - UNDER_REVIEW to REJECTED is valid")
    void updateStatus_underReviewToRejected() {
        draftApp.setStatus(ApplicationStatus.UNDER_REVIEW);
        LoanApplicationStatusRequest req = new LoanApplicationStatusRequest();
        req.setStatus(ApplicationStatus.REJECTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplication result = loanApplicationService.updateStatus(1L, req);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("updateStatus() - invalid transition DRAFT to APPROVED should throw")
    void updateStatus_invalidTransition_throwsBadRequest() {
        draftApp.setStatus(ApplicationStatus.DRAFT);
        LoanApplicationStatusRequest req = new LoanApplicationStatusRequest();
        req.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));

        assertThatThrownBy(() -> loanApplicationService.updateStatus(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("updateStatus() - invalid transition SUBMITTED to DISBURSED should throw")
    void updateStatus_submittedToDisbursed_throwsBadRequest() {
        draftApp.setStatus(ApplicationStatus.SUBMITTED);
        LoanApplicationStatusRequest req = new LoanApplicationStatusRequest();
        req.setStatus(ApplicationStatus.DISBURSED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApp));

        assertThatThrownBy(() -> loanApplicationService.updateStatus(1L, req))
                .isInstanceOf(BadRequestException.class);
    }


    @Test
    @DisplayName("getAll() - should return all applications")
    void getAll_returnsList() {
        when(applicationRepository.findAll()).thenReturn(List.of(draftApp));

        List<LoanApplication> result = loanApplicationService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getById() - should throw when not found")
    void getById_notFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getByBorrower() - should return applications for a borrower")
    void getByBorrower_returnsApps() {
        when(applicationRepository.findByBorrowerID(1L)).thenReturn(List.of(draftApp));

        List<LoanApplication> result = loanApplicationService.getByBorrower(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBorrowerID()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByStatus() - should filter by status")
    void getByStatus_filtered() {
        when(applicationRepository.findByStatus(ApplicationStatus.DRAFT))
                .thenReturn(List.of(draftApp));

        List<LoanApplication> result = loanApplicationService.getByStatus(ApplicationStatus.DRAFT);

        assertThat(result).allMatch(a -> a.getStatus() == ApplicationStatus.DRAFT);
    }
}
