package com.microlend.repository;

import com.microlend.entity.Borrower;
import com.microlend.enums.BorrowerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    Optional<Borrower> findByNationalIDNumber(String nationalIDNumber);

    Optional<Borrower> findByEmail(String email);

    List<Borrower> findByStatus(BorrowerStatus status);

    List<Borrower> findByVillage(String village);
}