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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuditLogService auditLogService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (!principal.getRole().equals("ADMIN")) {

            // Clear security context
            request.getSession().invalidate();

            request.getSession(true).setAttribute(
                    "notify",
                    new Notification(
                            NotifyType.ERROR,
                            "Tài khoản không có quyền truy cập !"
                    )
            );

            response.sendRedirect("/admin/auth/login");
            return;
        }

        auditLogService.logSimple(
                Action.LOGIN,
                "ADMIN_LOGIN",
                principal.getId(),
                principal.getUsername(),
                principal.getId(),
                principal.getUsername(),
                principal.getRole(),
                request
        );

        request.getSession().setAttribute(
                "notify",
                new Notification(
                        NotifyType.SUCCESS,
                        "Đăng nhập thành công! Chào mừng quản trị viên."
                )
        );

        response.sendRedirect("/admin/dashboard/index");
    }

}
