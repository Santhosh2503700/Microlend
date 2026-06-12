package com.microlend.repository;

import com.microlend.entity.LoanApplication;
import com.microlend.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByBorrowerID(Long borrowerID);
    List<LoanApplication> findByStatus(ApplicationStatus status);
    List<LoanApplication> findByCreditOfficerID(Long creditOfficerID);
    List<LoanApplication> findByGroupID(Long groupID);
}
