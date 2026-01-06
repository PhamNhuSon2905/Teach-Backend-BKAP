package com.bkap.teach.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminProfileRequest {
    @NotBlank(message = "Họ và tên không được để trống!")
    private String fullname;

    @NotBlank(message = "Email không được để trống!")
    @Email(message = "Email không hợp lệ!")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống!")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại không hợp lệ!")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống!")
    private String address;

    private String avatar;
}
