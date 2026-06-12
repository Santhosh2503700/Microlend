package com.microlend.service;

import com.microlend.dto.request.BorrowerRequest;
import com.microlend.entity.Borrower;
import com.microlend.enums.BorrowerStatus;
import com.microlend.exception.BadRequestException;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;

    public Borrower create(BorrowerRequest req) {
        if (req.getNationalIDNumber() != null &&
                borrowerRepository.findByNationalIDNumber(req.getNationalIDNumber()).isPresent()) {
            throw new BadRequestException("Borrower with National ID already exists");
        }
        Borrower borrower = Borrower.builder()
                .name(req.getName())
                .dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender())
                .nationalIDNumber(req.getNationalIDNumber())
                .village(req.getVillage())
                .district(req.getDistrict())
                .phone(req.getPhone())
                .occupation(req.getOccupation())
                .monthlyIncome(req.getMonthlyIncome())
                .bankAccountNumber(req.getBankAccountNumber())
                .status(req.getStatus() != null ? req.getStatus() : BorrowerStatus.ACTIVE)
                .build();
        return borrowerRepository.save(borrower);
    }

    public List<Borrower> getAll() {
        return borrowerRepository.findAll();
    }

    public Borrower getById(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found with ID: " + id));
    }

    public Borrower update(Long id, BorrowerRequest req) {
        Borrower borrower = getById(id);
        borrower.setName(req.getName());
        if (req.getDateOfBirth() != null) borrower.setDateOfBirth(req.getDateOfBirth());
        if (req.getGender() != null) borrower.setGender(req.getGender());
        if (req.getVillage() != null) borrower.setVillage(req.getVillage());
        if (req.getDistrict() != null) borrower.setDistrict(req.getDistrict());
        if (req.getPhone() != null) borrower.setPhone(req.getPhone());
        if (req.getOccupation() != null) borrower.setOccupation(req.getOccupation());
        if (req.getMonthlyIncome() != null) borrower.setMonthlyIncome(req.getMonthlyIncome());
        if (req.getBankAccountNumber() != null) borrower.setBankAccountNumber(req.getBankAccountNumber());
        if (req.getStatus() != null) borrower.setStatus(req.getStatus());
        return borrowerRepository.save(borrower);
    }

    public void delete(Long id) {
        getById(id);
        borrowerRepository.deleteById(id);
    }

    public List<Borrower> getByStatus(BorrowerStatus status) {
        return borrowerRepository.findByStatus(status);
    }
}
