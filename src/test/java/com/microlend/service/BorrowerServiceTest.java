package com.microlend.service;

import com.microlend.dto.request.BorrowerRequest;
import com.microlend.entity.Borrower;
import com.microlend.enums.BorrowerStatus;
import com.microlend.exception.BadRequestException;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.BorrowerRepository;
import com.microlend.repository.UserRepository;
import com.microlend.service.impl.BorrowerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowerService Tests")
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private BorrowerServiceImpl borrowerService;

    private BorrowerRequest validRequest;
    private Borrower savedBorrower;

    @BeforeEach
    void setUp() {
        validRequest = new BorrowerRequest();
        validRequest.setName("Lakshmi Devi");
        validRequest.setDateOfBirth(LocalDate.of(1990, 5, 15));
        validRequest.setGender("Female");
        validRequest.setNationalIDNumber("AADHAAR-1234-5678");
        validRequest.setVillage("Greenwood");
        validRequest.setDistrict("Raichur");
        validRequest.setPhone("9845001234");
        validRequest.setOccupation("Farmer");
        validRequest.setMonthlyIncome(new BigDecimal("12000"));
        validRequest.setBankAccountNumber("SBI0001234567");

        savedBorrower = Borrower.builder()
                .borrowerID(1L)
                .name("Lakshmi Devi")
                .nationalIDNumber("AADHAAR-1234-5678")
                .village("Greenwood")
                .monthlyIncome(new BigDecimal("12000"))
                .status(BorrowerStatus.ACTIVE)
                .build();
    }


    @Test
    @DisplayName("create() - should save and return borrower with ACTIVE status")
    void create_success() {
        when(borrowerRepository.findByNationalIDNumber("AADHAAR-1234-5678"))
                .thenReturn(Optional.empty());
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(savedBorrower);

        Borrower result = borrowerService.create(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getBorrowerID()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Lakshmi Devi");
        assertThat(result.getStatus()).isEqualTo(BorrowerStatus.ACTIVE);
        verify(borrowerRepository, times(1)).save(any(Borrower.class));
    }

    @Test
    @DisplayName("create() - should throw BadRequestException when National ID already exists")
    void create_duplicateNationalId_throwsBadRequest() {
        when(borrowerRepository.findByNationalIDNumber("AADHAAR-1234-5678"))
                .thenReturn(Optional.of(savedBorrower));

        assertThatThrownBy(() -> borrowerService.create(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("National ID already exists");

        verify(borrowerRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() - should use ACTIVE as default status when not provided")
    void create_defaultsToActiveStatus() {
        validRequest.setStatus(null);
        when(borrowerRepository.findByNationalIDNumber(any())).thenReturn(Optional.empty());
        when(borrowerRepository.save(any(Borrower.class))).thenAnswer(inv -> {
            Borrower b = inv.getArgument(0);
            assertThat(b.getStatus()).isEqualTo(BorrowerStatus.ACTIVE);
            return savedBorrower;
        });

        borrowerService.create(validRequest);

        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    @DisplayName("create() - should work when nationalIDNumber is null (no duplicate check)")
    void create_nullNationalId_skipsDuplicateCheck() {
        validRequest.setNationalIDNumber(null);
        when(borrowerRepository.save(any())).thenReturn(savedBorrower);

        Borrower result = borrowerService.create(validRequest);

        assertThat(result).isNotNull();
        verify(borrowerRepository, never()).findByNationalIDNumber(any());
    }


    @Test
    @DisplayName("getAll() - should return list of all borrowers")
    void getAll_returnsList() {
        Borrower b2 = Borrower.builder().borrowerID(2L).name("Sunita Patil").build();
        when(borrowerRepository.findAll()).thenReturn(List.of(savedBorrower, b2));

        List<Borrower> result = borrowerService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Borrower::getName)
                .containsExactly("Lakshmi Devi", "Sunita Patil");
    }

    @Test
    @DisplayName("getAll() - should return empty list when no borrowers exist")
    void getAll_emptyList() {
        when(borrowerRepository.findAll()).thenReturn(List.of());

        List<Borrower> result = borrowerService.getAll();

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("getById() - should return borrower when ID exists")
    void getById_found() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(savedBorrower));

        Borrower result = borrowerService.getById(1L);

        assertThat(result.getBorrowerID()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Lakshmi Devi");
    }

    @Test
    @DisplayName("getById() - should throw ResourceNotFoundException when ID not found")
    void getById_notFound_throwsException() {
        when(borrowerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowerService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }


    @Test
    @DisplayName("update() - should update and return modified borrower")
    void update_success() {
        validRequest.setPhone("9999999999");
        validRequest.setMonthlyIncome(new BigDecimal("15000"));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(savedBorrower));
        when(borrowerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Borrower result = borrowerService.update(1L, validRequest);

        assertThat(result.getPhone()).isEqualTo("9999999999");
        assertThat(result.getMonthlyIncome()).isEqualByComparingTo("15000");
    }

    @Test
    @DisplayName("update() - should throw when borrower not found")
    void update_notFound_throwsException() {
        when(borrowerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowerService.update(99L, validRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    @DisplayName("delete() - should call deleteById when borrower exists")
    void delete_success() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(savedBorrower));

        borrowerService.delete(1L);

        verify(borrowerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete() - should throw when borrower not found")
    void delete_notFound_throwsException() {
        when(borrowerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowerService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(borrowerRepository, never()).deleteById(any());
    }


    @Test
    @DisplayName("getByStatus() - should return borrowers filtered by status")
    void getByStatus_returnsFiltered() {
        when(borrowerRepository.findByStatus(BorrowerStatus.ACTIVE))
                .thenReturn(List.of(savedBorrower));

        List<Borrower> result = borrowerService.getByStatus(BorrowerStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BorrowerStatus.ACTIVE);
    }
}
