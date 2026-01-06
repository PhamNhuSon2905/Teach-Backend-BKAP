package com.bkap.teach.admin.controller;

import com.bkap.teach.admin.service.AdminLessonService;
import com.bkap.teach.admin.service.AdminLessonTeacherService;
import com.bkap.teach.audit.AuditAnotation;
import com.bkap.teach.common.notify.Notify;
import com.bkap.teach.entity.Lesson;
import com.bkap.teach.enums.Action;
import com.bkap.teach.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/lesson")
@PreAuthorize("hasAuthority('ADMIN')")

public class AdminLessonAssignController {

    @Autowired
    private AdminLessonService lessonService;
    @Autowired
    private AdminLessonTeacherService adminLessonTeacherService;
    @Autowired
    private UserService userService;


    @GetMapping("/{id}/assign")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "LESSON_ASSIGN_LIST"
    )
    public String assignPage(@PathVariable Long id, Model model) {

        Lesson lesson = lessonService.findById(id);

        model.addAttribute("lesson", lesson);
        model.addAttribute(
                "teachers",
                userService.findAllTeachers()
        );
        model.addAttribute(
                "assignedTeacherIds",
                adminLessonTeacherService.getAssignedTeacherIds(id)
        );

        return "admin/lesson/assign";
    }

    @PostMapping("/{id}/assign")
    @AuditAnotation(
            action = Action.UPDATE,
            objectType = "LESSON_ASSIGN"
    )
    public String assignSubmit(
            @PathVariable Long id,
            @RequestParam(required = false) List<Long> teacherIds,
            RedirectAttributes redirectAttributes
    ) {

        try {
            adminLessonTeacherService.assignTeachers(id, teacherIds);
        } catch (IllegalArgumentException e) {

            Notify.error(redirectAttributes, e.getMessage());
            return "redirect:/admin/lesson/" + id + "/assign";
        }

        Notify.success(redirectAttributes, "Cập nhật bài giảng gán riêng cho giảng viên thành công !");

        return "redirect:/admin/lesson/index";
    }

}