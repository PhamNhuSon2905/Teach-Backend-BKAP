package com.bkap.teach.audit;

import com.bkap.teach.enums.Status;
import com.bkap.teach.security.UserPrincipal;
import com.bkap.teach.service.audit.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @AfterReturning("@annotation(audit)")
    public void afterSuccess(JoinPoint joinPoint, AuditAnotation audit) {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof UserPrincipal userPrincipal)) return;

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) return;

        HttpServletRequest request = attrs.getRequest();


        Object[] args = joinPoint.getArgs();

        Long targetUserId = null;
        String objectName = null;

        for (Object arg : args) {
            if (arg instanceof Long && targetUserId == null) {
                targetUserId = (Long) arg;
            }
            if (arg instanceof Status status) {
                objectName = "STATUS -> " + status.name();
            }
        }

        if (targetUserId == null) {
            targetUserId = userPrincipal.getId();
        }

        auditLogService.logSimple(
                audit.action(),
                audit.objectType(),
                targetUserId,
                objectName,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getRole(),
                request
        );
    }

}
