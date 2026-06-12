package com.microlend.entity;

import com.microlend.enums.GroupStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "borrower_groups")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BorrowerGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupID;

    @Column(nullable = false)
    private String groupName;

    private Long centreID;
    private Long fieldOfficerID;
    private LocalDate formationDate;

    @Builder.Default
    private Integer memberCount = 0;

    @Builder.Default
    private Boolean jointLiabilityEnabled = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;
}
