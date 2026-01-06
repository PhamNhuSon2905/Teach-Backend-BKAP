package com.bkap.teach.repository;

import com.bkap.teach.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    long count();
}
