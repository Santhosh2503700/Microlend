package com.microlend.entity;

import com.microlend.enums.KYCDocumentType;
import com.microlend.enums.KYCStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "borrower_kyc")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BorrowerKYC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kycID;

    @Column(nullable = false)
    private Long borrowerID;

    @Enumerated(EnumType.STRING)
    private KYCDocumentType documentType;

    private String documentRef;
    private Long verifiedByID;
    private LocalDate verificationDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KYCStatus status = KYCStatus.PENDING;
}
