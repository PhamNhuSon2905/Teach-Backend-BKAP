package com.bkap.teach.teacher.controller.lesson;

import com.bkap.teach.common.api.ApiResponse;
import com.bkap.teach.entity.User;
import com.bkap.teach.enums.Action;
import com.bkap.teach.audit.AuditAnotation;
import com.bkap.teach.service.lession.TeacherLessonService;
import com.bkap.teach.service.user.UserService;
import com.bkap.teach.teacher.dto.response.TeacherLessonContentResponse;
import com.bkap.teach.teacher.dto.response.TeacherLessonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@CrossOrigin(origins = "*")
@Tag(name = "Lesson API", description = "Quản lý bài giảng của giảng viên")
@RestController
@RequestMapping("/api/teacher/lessons")
@PreAuthorize("hasAuthority('TEACHER')")
public class TeacherLessonController {

    @Autowired
    private TeacherLessonService teacherLessonService;

    @Autowired
    private UserService userService;


    @GetMapping
    @Operation(summary = "Lấy danh sách bài giảng của giảng viên")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "TEACHER_LESSON_BY_ASSIGNED_GRADE_LIST"
    )
    public ApiResponse<List<TeacherLessonResponse>> getTeacherLessonsByAssignedGrades(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Integer teachingMonth
    ) {
        User teacher = userService.getCurrentUser();

        return ApiResponse.success(
                teacherLessonService.teacherLessonByAssignedGrades(
                        teacher.getId(),
                        keyword,
                        grade,
                        teachingMonth
                )
        );
    }

    @Operation(summary = "Lấy danh sách bài giảng của giảng viên được phân công riêng")
    @GetMapping("/assigned-list")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "TEACHER_ASSIGNED_LESSON_LIST"
    )
    public ApiResponse<List<TeacherLessonResponse>> getTeacherAssignedLessonList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer grade
    ) {
        User teacher = userService.getCurrentUser();

        return ApiResponse.success(
                teacherLessonService.teacherAssignedLessonList(
                        teacher.getId(),
                        keyword,
                        grade
                )
        );
    }

    @Operation(summary = "Xem nội dung bài giảng theo mã bài giảng")
    @GetMapping("/{lessonId}")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "TEACHER_LESSON_CONTENT"
    )
    public ApiResponse<TeacherLessonContentResponse> getLessonContent(
            @PathVariable Long lessonId
    ) {
        User teacher = userService.getCurrentUser();

        return ApiResponse.success(
                teacherLessonService.getLessonContent(
                        lessonId,
                        teacher.getId()
                )
        );
    }

}
