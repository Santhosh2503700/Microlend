package com.microlend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditID;

    private Long userID;
    private String action;
    private String module;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
