package com.bkap.teach.teacher.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TeacherProfileResponse {

    private Long id;
    private String username;
    private String fullname;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String status;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}