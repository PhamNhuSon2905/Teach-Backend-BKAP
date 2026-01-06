package com.bkap.teach.service.audit;

import com.bkap.teach.entity.AuditLog;
import com.bkap.teach.repository.AuditLogRepository;
import com.bkap.teach.enums.Action;
import com.bkap.teach.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void logSimple(
            Action action,
            String objectType,
            Long objectId,
            String objectName,
            Long userId,
            String username,
            String role,
            HttpServletRequest request
    ) {

        AuditLog audit = AuditLog.builder()
                .action(action)
                .objectType(objectType)
                .objectId(objectId)
                .objectName(objectName)
                .userId(userId)
                .username(username)
                .role(Role.valueOf(role))
                .ipAddress(getClientIp(request))
                .build();

        auditLogRepository.save(audit);
    }
    // lay dia chi ip cua thiet bi
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // danh sach log voi role admin
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<AuditLog> findAll(int pageNo, int pageSize) {

        Pageable pageable = PageRequest.of(
                pageNo - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        return auditLogRepository.findAll(pageable);
    }


}
