package com.bkap.teach.teacher.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherLessonResponse {

    private Long id;
    private String code;
    private String name;
    private Integer grade;
    private Integer teachingMonth;
    private String coverImage;
}
