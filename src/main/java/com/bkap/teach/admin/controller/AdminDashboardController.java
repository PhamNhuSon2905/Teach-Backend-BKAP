package com.bkap.teach.admin.controller;


import com.bkap.teach.admin.service.AdminDashboardService;
import com.bkap.teach.audit.AuditAnotation;
import com.bkap.teach.enums.Action;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminDashboardController {

     @Autowired
     private AdminDashboardService adminDashboardService;

    @GetMapping("/index")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "DASHBOARD_VIEW"
    )
    public String dashboard(HttpServletRequest request, Model model) {
        Object notify = request.getSession().getAttribute("notify");
        if (notify != null) {
            model.addAttribute("notify", notify);
            request.getSession().removeAttribute("notify");
        }
        model.addAttribute("totalUsers", adminDashboardService.totalUsers());
        model.addAttribute("totalTeachers", adminDashboardService.totalTeachers());
        model.addAttribute("activeUsers", adminDashboardService.activeUsers());
        model.addAttribute("lockedUsers", adminDashboardService.lockedUsers());
        model.addAttribute("totalLessons", adminDashboardService.totalLessons());
        model.addAttribute("totalLogs", adminDashboardService.totalAuditLogs());

        return "admin/dashboard/index";
    }
}
