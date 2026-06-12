package com.microlend.entity;

import com.microlend.enums.BorrowerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "borrowers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long borrowerID;

    @Column(nullable = false)
    private String name;

    private LocalDate dateOfBirth;
    private String gender;

    @Column(unique = true)
    private String nationalIDNumber;

    private String village;
    private String district;
    private String phone;
    private String occupation;
    private BigDecimal monthlyIncome;
    private String bankAccountNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BorrowerStatus status = BorrowerStatus.ACTIVE;
}
