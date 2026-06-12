package com.microlend.repository;

import com.microlend.entity.CollectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CollectionRecordRepository extends JpaRepository<CollectionRecord, Long> {
    List<CollectionRecord> findByLoanAccountID(Long loanAccountID);
    List<CollectionRecord> findByCollectedByID(Long collectedByID);
}
