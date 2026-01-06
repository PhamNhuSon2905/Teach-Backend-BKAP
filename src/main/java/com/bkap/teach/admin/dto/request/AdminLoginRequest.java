package com.bkap.teach.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class AdminLoginRequest {
    @NotBlank(message = "Vui lòng nhập tên người dùng để đăng nhập!")
    private String username;

    @NotBlank(message = "Vui lòng nhập mật khẩu để đăng nhập!")
    private String password;
}
