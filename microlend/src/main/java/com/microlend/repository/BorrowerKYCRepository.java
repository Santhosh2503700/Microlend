package com.microlend.repository;

import com.microlend.entity.BorrowerKYC;
import com.microlend.enums.KYCStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BorrowerKYCRepository extends JpaRepository<BorrowerKYC, Long> {
    List<BorrowerKYC> findByBorrowerID(Long borrowerID);
    List<BorrowerKYC> findByStatus(KYCStatus status);
}
