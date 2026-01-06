package com.bkap.teach.teacher.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class TeacherLessonContentResponse {

    private Long id;
    private String code;
    private String name;
    private Integer grade;
    private Integer teachingMonth;
    private String description;
    private String coverImage;
    private List<LessonFileResponse> files;
}
