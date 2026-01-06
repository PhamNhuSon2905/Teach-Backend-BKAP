package com.bkap.teach.teacher.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRegisterRequest {
    @NotBlank(message = "Tên người dùng không được để trống!")
    private String username;

    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự!")
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống!")
    private String fullname;

    @Email(message = " Địa chỉ email không hợp lệ!")
    @NotBlank(message = "Email không được để trống!")
    private String email;

    @NotBlank(message = "Địa chỉ không được để trống!")
    private String address;

    @NotBlank(message = "Số điện thoại không được để trống!")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại không hợp lệ!")
    private String phone;
}
