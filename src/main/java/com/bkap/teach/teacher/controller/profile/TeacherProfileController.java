package com.bkap.teach.teacher.controller.profile;

import com.bkap.teach.common.api.ApiResponse;
import com.bkap.teach.entity.User;
import com.bkap.teach.service.teacher.TeacherAccountService;
import com.bkap.teach.service.teacher.TeacherProfileService;
import com.bkap.teach.service.user.UserService;
import com.bkap.teach.teacher.dto.request.TeacherChangePasswordRequest;
import com.bkap.teach.teacher.dto.request.TeacherUpdateProfileRequest;
import com.bkap.teach.teacher.dto.response.TeacherProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin(origins = "*")
@Tag(name = "Profile API", description = "Quản lý thông tin cá nhân giảng viên")
@RestController
@RequestMapping("api/teacher/profile")
@PreAuthorize("hasAuthority('TEACHER')")
public class TeacherProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherAccountService teacherAccountService;

    @Autowired
    private TeacherProfileService teacherProfileService;


    @Operation(summary = "Thông tin của giảng viên đang đăng nhập vào hệ thống")
    @GetMapping("/me")
    public ApiResponse<TeacherProfileResponse> me() {
        User user = userService.getCurrentUser();
        return ApiResponse.success(teacherProfileService.toProfileResponse(user));
    }



    @Operation(summary = "Cập nhật thông tin & ảnh đại diện giảng viên")
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<TeacherProfileResponse> updateProfile(
            @RequestPart("data") @Valid TeacherUpdateProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    )
    {
        User user = userService.getCurrentUser();

        TeacherProfileResponse res =
                teacherProfileService.updateProfile(
                        user.getId(),
                        request,
                        avatar
                );

        return ApiResponse.success("Cập nhật thông tin thành công", res);
    }

    @Operation(summary = "Tiến hành đổi mật khẩu dành cho giảng viên")
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody TeacherChangePasswordRequest request
    ) {
        try {
            User teacher = userService.getCurrentUser();
            teacherAccountService.changePassword(teacher.getId(), request);
            return ApiResponse.success("Đổi mật khẩu thành công", null);

        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }


}


