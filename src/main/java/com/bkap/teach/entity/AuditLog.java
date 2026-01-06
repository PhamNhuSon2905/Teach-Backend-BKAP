package com.bkap.teach.entity;

import com.bkap.teach.enums.Action;
import com.bkap.teach.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Action action;

    private String objectType;
    private Long objectId;
    private String objectName;

    @Column(name = "user_id")
    private Long userId;

    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String ipAddress;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }



}
