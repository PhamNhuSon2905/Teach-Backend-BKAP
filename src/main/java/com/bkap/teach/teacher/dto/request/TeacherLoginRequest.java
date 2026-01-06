package com.bkap.teach.teacher.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherLoginRequest {
    @NotBlank(message = "Vui lòng nhập tên người dùng!")
    private String username;

    @NotBlank(message = "Vui lòng nhập mật khẩu!")
    private String password;
}
