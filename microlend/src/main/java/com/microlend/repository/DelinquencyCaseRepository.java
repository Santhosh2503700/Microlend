package com.microlend.repository;

import com.microlend.entity.DelinquencyCase;
import com.microlend.enums.DelinquencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DelinquencyCaseRepository extends JpaRepository<DelinquencyCase, Long> {
    List<DelinquencyCase> findByLoanAccountID(Long loanAccountID);
    List<DelinquencyCase> findByStatus(DelinquencyStatus status);
    List<DelinquencyCase> findByAssignedCollectionsOfficerID(Long officerID);

    /**
     * BUG FIX #5: Used by DelinquencyScheduler to check if an open case already
     * exists for a given account before creating a duplicate.
     */
    Optional<DelinquencyCase> findByLoanAccountIDAndStatus(Long loanAccountID,
                                                             DelinquencyStatus status);
}
