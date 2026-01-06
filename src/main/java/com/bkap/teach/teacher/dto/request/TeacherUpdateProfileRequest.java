package com.bkap.teach.teacher.dto.request;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TeacherUpdateProfileRequest {
    private String fullname;
    private String phone;
    private String address;
    private String email;
}
