package com.bkap.teach.security.handler;

import com.bkap.teach.common.notify.Notification;
import com.bkap.teach.common.notify.NotifyType;
import com.bkap.teach.enums.Action;
import com.bkap.teach.security.UserPrincipal;
import com.bkap.teach.service.audit.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final AuditLogService auditLogService;

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {

            auditLogService.logSimple(
                    Action.LOGOUT,
                    "ADMIN_LOGOUT",
                    principal.getId(),
                    principal.getUsername(),
                    principal.getId(),
                    principal.getUsername(),
                    principal.getRole(),
                    request
            );
        }

        request.getSession().setAttribute(
                "notify",
                new Notification(
                        NotifyType.WARNING,
                        "Đã đăng xuất tài khoản!"
                )
        );

        response.sendRedirect("/admin/auth/login");
    }
}
