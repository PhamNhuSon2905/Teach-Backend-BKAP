package com.bkap.teach.admin.controller;

import com.bkap.teach.audit.AuditAnotation;
import com.bkap.teach.entity.AuditLog;
import com.bkap.teach.enums.Action;
import com.bkap.teach.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit-logs")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class AdminAuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/index")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "AUDIT_LOG_LIST"
    )
    public String index(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model
    ) {

        Page<AuditLog> page = auditLogService.findAll(pageNo, pageSize);

        model.addAttribute("logs", page.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPage", page.getTotalPages());

        return "admin/audit-logs/index";
    }


}

