package com.microlend.entity;

import com.microlend.enums.CollectionMode;
import com.microlend.enums.CollectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "collection_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collectionID;

    @Column(nullable = false)
    private Long loanAccountID;

    private Long scheduleID;
    private BigDecimal collectedAmount;

    @Builder.Default
    private LocalDate collectionDate = LocalDate.now();

    private Long collectedByID;

    @Enumerated(EnumType.STRING)
    private CollectionMode mode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CollectionStatus status = CollectionStatus.RECEIVED;
}
