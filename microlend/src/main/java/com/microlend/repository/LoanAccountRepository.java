package com.microlend.repository;

import com.microlend.entity.LoanAccount;
import com.microlend.enums.LoanAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {
    List<LoanAccount> findByBorrowerID(Long borrowerID);
    List<LoanAccount> findByStatus(LoanAccountStatus status);
    Optional<LoanAccount> findByApplicationID(Long applicationID);
}
