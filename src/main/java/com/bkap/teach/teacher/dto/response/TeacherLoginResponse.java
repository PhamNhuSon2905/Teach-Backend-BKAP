package com.bkap.teach.teacher.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class TeacherLoginResponse {

    private String token;
    private Long id;
    private String username;
    private String fullname;
    private String role;
    private String avatar;
}

