package com.bkap.teach.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminChangePasswordRequest {
    @NotBlank(message = "Mật khẩu hiện tại không được để trống!")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống!")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống!")
    private String confirmPassword;

}
