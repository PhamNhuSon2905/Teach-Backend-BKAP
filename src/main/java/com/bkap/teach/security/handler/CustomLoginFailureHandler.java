package com.bkap.teach.security.handler;

import com.bkap.teach.common.notify.Notification;
import com.bkap.teach.common.notify.NotifyType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        NotifyType type;
        String message;

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            type = NotifyType.WARNING;
            message = "Vui lòng điền đầy đủ tên đăng nhập và mật khẩu !";
        } else {
            type = NotifyType.ERROR;
            message = "Tên đăng nhập hoặc mật khẩu không chính xác!";
        }

        request.getSession().setAttribute(
                "notify",
                new Notification(type, message)
        );

        response.sendRedirect("/admin/auth/login");
    }
}
