package com.bkap.teach.teacher.controller.auth;

import com.bkap.teach.common.api.ApiResponse;
import com.bkap.teach.security.UserPrincipal;
import com.bkap.teach.security.jwt.JwtUtil;
import com.bkap.teach.teacher.dto.request.TeacherLoginRequest;
import com.bkap.teach.teacher.dto.response.TeacherLoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@Tag(name = "Auth API", description = "Quản lý truy cập giảng viên")
@RestController
@RequestMapping("api/teacher/auth")
public class TeacherAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;


    @Operation(summary = "Giảng viên đăng nhập vào hệ thống")
    @PostMapping("/login")
    public ApiResponse<TeacherLoginResponse> login(
            @Valid @RequestBody TeacherLoginRequest request
    ) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (!"TEACHER".equals(principal.getRole())) {
            return ApiResponse.error("Tài khoản này không phải giảng viên !");
        }

        String token = jwtUtil.generateToken(principal);

        return ApiResponse.success(
                "Đăng nhập thành công !",
                new TeacherLoginResponse(
                        token,
                        principal.getId(),
                        principal.getUsername(),
                        principal.getFullName(),
                        principal.getRole(),
                        principal.getAvatar() != null && !principal.getAvatar().isBlank()
                                ? principal.getAvatar()
                                : "/assets/images/default_teacher.jpg"
                )
        );
    }



}
