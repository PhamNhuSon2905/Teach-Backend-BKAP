package com.bkap.teach.teacher.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherChangePasswordRequest {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống !")
    private String currentPassword;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới !")
    private String newPassword;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu mới !")
    private String confirmNewPassword;
}
