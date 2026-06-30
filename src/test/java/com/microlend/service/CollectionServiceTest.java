package com.microlend.service;

import com.microlend.dto.request.CollectionRequest;
import com.microlend.entity.CollectionRecord;
import com.microlend.entity.LoanAccount;
import com.microlend.entity.RepaymentSchedule;
import com.microlend.enums.*;
import com.microlend.exception.BadRequestException;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.CollectionRecordRepository;
import com.microlend.repository.LoanAccountRepository;
import com.microlend.repository.RepaymentScheduleRepository;
import com.microlend.service.impl.CollectionServiceImpl;
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
@DisplayName("CollectionService Tests — includes Bug Fix #4 verification")
class CollectionServiceTest {

    @Mock private CollectionRecordRepository collectionRepository;
    @Mock private RepaymentScheduleRepository scheduleRepository;
    @Mock private LoanAccountRepository loanAccountRepository;
    @InjectMocks private CollectionServiceImpl collectionService;

    private LoanAccount activeAccount;
    private RepaymentSchedule pendingSchedule;
    private CollectionRequest fullPaymentRequest;
    private CollectionRequest partialPaymentRequest;

    @BeforeEach
    void setUp() {
        activeAccount = LoanAccount.builder()
                .loanAccountID(1L)
                .borrowerID(1L)
                .disbursedAmount(new BigDecimal("25000"))
                .outstandingPrincipal(new BigDecimal("25000"))
                .status(LoanAccountStatus.ACTIVE)
                .dpd(0)
                .build();

        pendingSchedule = RepaymentSchedule.builder()
                .scheduleID(1L)
                .loanAccountID(1L)
                .installmentNumber(1)
                .dueDate(LocalDate.of(2024, 2, 20))
                .principalDue(new BigDecimal("1969.92"))
                .interestDue(new BigDecimal("250.00"))
                .totalDue(new BigDecimal("2219.92"))
                .amountPaid(BigDecimal.ZERO)
                .status(InstallmentStatus.PENDING)
                .build();

        fullPaymentRequest = new CollectionRequest();
        fullPaymentRequest.setLoanAccountID(1L);
        fullPaymentRequest.setScheduleID(1L);
        fullPaymentRequest.setCollectedAmount(new BigDecimal("2219.92"));
        fullPaymentRequest.setCollectionDate(LocalDate.of(2024, 2, 20));
        fullPaymentRequest.setCollectedByID(4L);
        fullPaymentRequest.setMode(CollectionMode.CENTRE_COLLECTION);

        partialPaymentRequest = new CollectionRequest();
        partialPaymentRequest.setLoanAccountID(1L);
        partialPaymentRequest.setScheduleID(1L);
        partialPaymentRequest.setCollectedAmount(new BigDecimal("1000.00"));
        partialPaymentRequest.setCollectionDate(LocalDate.of(2024, 3, 20));
        partialPaymentRequest.setCollectedByID(4L);
        partialPaymentRequest.setMode(CollectionMode.CENTRE_COLLECTION);
    }


    @Test
    @DisplayName("BUG FIX #4 — Full payment: installment status becomes PAID")
    void recordCollection_fullPayment_installmentBecomePaid() {
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(pendingSchedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        collectionService.recordCollection(fullPaymentRequest);

        verify(scheduleRepository).save(argThat(s ->
                s.getStatus() == InstallmentStatus.PAID
                && s.getAmountPaid().compareTo(new BigDecimal("2219.92")) == 0
                && s.getPaidDate() != null
        ));
    }

    @Test
    @DisplayName("BUG FIX #4 — Full payment: collection record status is RECEIVED")
    void recordCollection_fullPayment_collectionStatusReceived() {
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(pendingSchedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CollectionRecord result = collectionService.recordCollection(fullPaymentRequest);

        assertThat(result.getStatus()).isEqualTo(CollectionStatus.RECEIVED);
    }

    @Test
    @DisplayName("BUG FIX #4 — Full payment: outstanding principal is reduced")
    void recordCollection_fullPayment_principalReduced() {
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(pendingSchedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        collectionService.recordCollection(fullPaymentRequest);

        verify(loanAccountRepository).save(argThat(a ->
                a.getOutstandingPrincipal().compareTo(
                    new BigDecimal("25000").subtract(new BigDecimal("2219.92"))) == 0
        ));
    }


    @Test
    @DisplayName("BUG FIX #4 — Partial payment: installment status MUST be PARTIAL, NOT PENDING")
    void recordCollection_partialPayment_installmentBecomesPartialNotPending() {
        
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(pendingSchedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        collectionService.recordCollection(partialPaymentRequest);

        verify(scheduleRepository).save(argThat(s -> {
            assertThat(s.getStatus())
                .as("Installment status MUST be PARTIAL after partial payment, NOT PENDING")
                .isEqualTo(InstallmentStatus.PARTIAL);
            assertThat(s.getAmountPaid())
                .as("amountPaid must be updated to 1000.00")
                .isEqualByComparingTo("1000.00");
            return true;
        }));
    }

    @Test
    @DisplayName("BUG FIX #4 — Partial payment: collection record status is PARTIAL")
    void recordCollection_partialPayment_collectionStatusIsPartial() {
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(pendingSchedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CollectionRecord result = collectionService.recordCollection(partialPaymentRequest);

        assertThat(result.getStatus()).isEqualTo(CollectionStatus.PARTIAL);
    }

    @Test
    @DisplayName("BUG FIX #4 — Partial payments accumulate: second partial adds to amountPaid")
    void recordCollection_secondPartialPayment_accumulatesAmountPaid() {
        pendingSchedule.setAmountPaid(new BigDecimal("1000.00"));
        pendingSchedule.setStatus(InstallmentStatus.PARTIAL);

        partialPaymentRequest.setCollectedAmount(new BigDecimal("500.00"));

        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(pendingSchedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        collectionService.recordCollection(partialPaymentRequest);

        verify(scheduleRepository).save(argThat(s ->
                s.getAmountPaid().compareTo(new BigDecimal("1500.00")) == 0
                && s.getStatus() == InstallmentStatus.PARTIAL
        ));
    }

    @Test
    @DisplayName("BUG FIX #4 — Final partial completes installment: status becomes PAID")
    void recordCollection_finalPartialCompletes_becomePaid() {
        pendingSchedule.setAmountPaid(new BigDecimal("1000.00"));
        pendingSchedule.setStatus(InstallmentStatus.PARTIAL);

        partialPaymentRequest.setCollectedAmount(new BigDecimal("1219.92")); // exactly remaining

        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(pendingSchedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        collectionService.recordCollection(partialPaymentRequest);

        verify(scheduleRepository).save(argThat(s ->
                s.getStatus() == InstallmentStatus.PAID
                && s.getAmountPaid().compareTo(new BigDecimal("2219.92")) == 0
        ));
    }


    @Test
    @DisplayName("Excess payment: collection status is EXCESS")
    void recordCollection_excessPayment_statusIsExcess() {
        fullPaymentRequest.setCollectedAmount(new BigDecimal("2500.00")); // more than 2219.92
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(pendingSchedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CollectionRecord result = collectionService.recordCollection(fullPaymentRequest);

        assertThat(result.getStatus()).isEqualTo(CollectionStatus.EXCESS);
    }


    @Test
    @DisplayName("Zero amount throws BadRequestException")
    void recordCollection_zeroAmount_throwsBadRequest() {
        fullPaymentRequest.setCollectedAmount(BigDecimal.ZERO);
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> collectionService.recordCollection(fullPaymentRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("Negative amount throws BadRequestException")
    void recordCollection_negativeAmount_throwsBadRequest() {
        fullPaymentRequest.setCollectedAmount(new BigDecimal("-100"));
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> collectionService.recordCollection(fullPaymentRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Null amount throws BadRequestException")
    void recordCollection_nullAmount_throwsBadRequest() {
        fullPaymentRequest.setCollectedAmount(null);
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> collectionService.recordCollection(fullPaymentRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Closed account throws BadRequestException")
    void recordCollection_closedAccount_throwsBadRequest() {
        activeAccount.setStatus(LoanAccountStatus.CLOSED);
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> collectionService.recordCollection(fullPaymentRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("closed");
    }

    @Test
    @DisplayName("Account not found throws ResourceNotFoundException")
    void recordCollection_accountNotFound_throwsNotFound() {
        when(loanAccountRepository.findById(99L)).thenReturn(Optional.empty());
        fullPaymentRequest.setLoanAccountID(99L);

        assertThatThrownBy(() -> collectionService.recordCollection(fullPaymentRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Schedule not found throws ResourceNotFoundException")
    void recordCollection_scheduleNotFound_throwsNotFound() {
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());
        fullPaymentRequest.setScheduleID(99L);

        assertThatThrownBy(() -> collectionService.recordCollection(fullPaymentRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Account closes automatically when outstanding principal reaches zero")
    void recordCollection_accountClosesWhenFullyPaid() {
        activeAccount.setOutstandingPrincipal(new BigDecimal("2219.92")); // exact last payment
        when(loanAccountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(pendingSchedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        collectionService.recordCollection(fullPaymentRequest);

        verify(loanAccountRepository).save(argThat(a ->
                a.getStatus() == LoanAccountStatus.CLOSED
                && a.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) == 0
        ));
    }


    @Test
    @DisplayName("getAll() - returns all collection records")
    void getAll_returnsList() {
        CollectionRecord c1 = CollectionRecord.builder().collectionID(1L).build();
        when(collectionRepository.findAll()).thenReturn(List.of(c1));

        List<CollectionRecord> result = collectionService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getByLoanAccount() - returns records for account")
    void getByLoanAccount_filtered() {
        CollectionRecord c1 = CollectionRecord.builder().collectionID(1L).loanAccountID(1L).build();
        when(collectionRepository.findByLoanAccountID(1L)).thenReturn(List.of(c1));

        List<CollectionRecord> result = collectionService.getByLoanAccount(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLoanAccountID()).isEqualTo(1L);
    }
}
