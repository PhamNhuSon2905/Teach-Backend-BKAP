package com.bkap.teach.security.handler;

import com.bkap.teach.common.notify.Notification;
import com.bkap.teach.common.notify.NotifyType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomInvalidSessionHandler implements InvalidSessionStrategy {

    @Override
    public void onInvalidSessionDetected(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.getSession(true).setAttribute(
                "notify",
                new Notification(
                        NotifyType.WARNING,
                        "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!"
                )
        );

        response.sendRedirect("/admin/auth/login");
    }
}
