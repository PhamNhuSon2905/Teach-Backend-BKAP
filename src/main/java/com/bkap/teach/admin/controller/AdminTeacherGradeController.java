package com.bkap.teach.admin.controller;

import com.bkap.teach.admin.service.AdminTeacherGradeService;
import com.bkap.teach.audit.AuditAnotation;
import com.bkap.teach.common.notify.Notify;
import com.bkap.teach.entity.User;
import com.bkap.teach.enums.Action;
import com.bkap.teach.repository.teacher.TeacherGradeHistoryRepository;
import com.bkap.teach.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/teacher")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminTeacherGradeController {

    @Autowired
    private AdminTeacherGradeService adminTeacherGradeService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherGradeHistoryRepository teacherGradeHistoryRepository;


    @GetMapping("/{id}/grades")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "TEACHER_GRADE_ASSIGN_LIST"
    )
    public String assignGradePage(
            @PathVariable("id") Long teacherId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            model.addAttribute(
                    "teacher",
                    userService.getUserDetail(teacherId)
            );

            var page = teacherGradeHistoryRepository
                    .findByTeacherIdOrderByActionAtDesc(
                            teacherId,
                            PageRequest.of(pageNo - 1, pageSize)
                    );

            model.addAttribute("histories", page.getContent());
            model.addAttribute("currentPage", pageNo);
            model.addAttribute("totalPage", page.getTotalPages());

            model.addAttribute(
                    "assignedGrades",
                    adminTeacherGradeService.getAssignedGrades(teacherId)
            );

            return "admin/teacher/assign-grade";

        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
            return "redirect:/admin/teacher/index";
        }
    }


    @PostMapping("/{id}/grades")
    @AuditAnotation(
            action = Action.UPDATE,
            objectType = "TEACHER_GRADE_ASSIGN"
    )
    public String assignGradeSubmit(
            @PathVariable("id") Long teacherId,
            @RequestParam(required = false) List<Integer> grades,
            RedirectAttributes ra
    ) {
        try {
            User currentAdmin = userService.getCurrentUser();

            adminTeacherGradeService.assignGrades(
                    teacherId,
                    grades,
                    currentAdmin
            );

            Notify.success(ra, "Cập nhật khối dạy cho giảng viên thành công !");
            return "redirect:/admin/teacher/index";

        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
            return "redirect:/admin/teacher/" + teacherId + "/grades";
        }
    }
}
