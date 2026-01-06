package com.bkap.teach.admin.controller;

import com.bkap.teach.audit.AuditAnotation;
import com.bkap.teach.enums.Action;
import com.bkap.teach.enums.Role;
import com.bkap.teach.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/teacher")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminTeacherController {

    @Autowired
    private UserService userService;

    @GetMapping("/index")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "TEACHER_LIST"
    )
    public String index(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        var page = userService.searchUsers(
                pageNo,
                pageSize,
                keyword,
                Role.TEACHER
        );

        model.addAttribute("teachers", page.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("keyword", keyword == null ? "" : keyword);

        return "admin/teacher/index";
    }
}
