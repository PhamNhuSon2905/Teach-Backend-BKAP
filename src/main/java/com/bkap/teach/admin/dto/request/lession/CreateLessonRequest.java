package com.bkap.teach.admin.dto.request.lession;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateLessonRequest {

    @NotBlank(message = "Mã bài giảng không được để trống!")
    @Size(max = 50, message = "Mã bài giảng tối đa 50 ký tự!")
    private String code;

    @NotBlank(message = "Tên bài giảng không được để trống!")
    @Size(max = 255, message = "Tên bài giảng quá dài!")
    private String name;

    @NotNull(message = "Lớp giảng dạy không được để trống!")
    @Min(value = 1, message = "Lớp giảng dạy phải từ 1 đến 12!")
    @Max(value = 12, message = "Lớp giảng dạy phải từ 1 đến 12!")
    private Integer grade;

    @NotNull(message = "Tháng giảng dạy không được để trống!")
    @Min(value = 1, message = "Tháng phải từ 1 đến 12!")
    @Max(value = 12, message = "Tháng phải từ 1 đến 12!")
    private Integer teachingMonth;

    @Size(max = 225, message = "Mô tả bài giảng quá dài!")
    @NotBlank(message = "Tên bài giảng không được để trống!")
    private String description;

    private MultipartFile coverFile;
}
