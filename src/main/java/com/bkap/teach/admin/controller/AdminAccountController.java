package com.bkap.teach.admin.controller;


import com.bkap.teach.teacher.dto.request.TeacherRegisterRequest;
import com.bkap.teach.audit.AuditAnotation;
import com.bkap.teach.common.notify.Notify;
import com.bkap.teach.entity.User;
import com.bkap.teach.enums.Action;
import com.bkap.teach.enums.Role;
import com.bkap.teach.enums.Status;
import com.bkap.teach.security.UserPrincipal;
import com.bkap.teach.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/account")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminAccountController {

    @Autowired
    private UserService userService;

    @GetMapping("/index")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "ACCOUNT_LIST"
    )
    public String index(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            Model model
    ) {

        var page = userService.searchUsers(pageNo, pageSize, keyword, role);

        model.addAttribute("users", page.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("role", role);

        return "admin/account/index";
    }


    @GetMapping("/create-teacher")
    public String createTeacherForm(Model model) {
        model.addAttribute("teacher", new TeacherRegisterRequest());
        return "admin/account/create-teacher";
    }


    @PostMapping("/create-teacher")
    @AuditAnotation(
            action = Action.CREATE,
            objectType = "TEACHER_ACCOUNT"
    )
    public String createTeacher(
            @Valid @ModelAttribute("teacher") TeacherRegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes ra
    ) {
        if (result.hasErrors()) {
            Notify.warning(model, "Vui lòng kiểm tra lại thông tin hồ sơ!");
            return "admin/account/create-teacher";
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

            userService.createTeacher(request, principal.getId());

        } catch (RuntimeException e) {
            Notify.error(model, e.getMessage());
            return "admin/account/create-teacher";
        }

        Notify.success(
                ra,"Tạo tài khoản giảng viên thành công: " + request.getUsername()
        );
        return "redirect:/admin/account/index";
    }


    @GetMapping("/{id}")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "ACCOUNT_DETAIL"
    )
    public String viewAccountDetail(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            User user = userService.getUserDetail(id);
            model.addAttribute("user", user);
            return "admin/account/detail";
        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
            return "redirect:/admin/account/index";
        }
    }





    @AuditAnotation(
            action = Action.UPDATE,
            objectType = "ACCOUNT_UPDATE_STATUS"
    )
    @PostMapping("/{id}/status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam Status status,
            RedirectAttributes ra
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

            userService.changeStatusWithRule(id, status, principal.getId());

            String msg = (status == Status.LOCKED)
                    ? "Khoá tài khoản giảng viên thành công !"
                    : "Mở khoá tài khoản giảng viên thành công !";

            Notify.success(ra, msg);

        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
        }

        return "redirect:/admin/account/index";
    }

    @AuditAnotation(
            action = Action.DELETE,
            objectType = "ACCOUNT"
    )
    @PostMapping("/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes ra
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

            userService.deleteWithRule(id, principal.getId());

            Notify.success(ra, "Xoá tài khoản giảng viên thành công !");

        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
        }

        return "redirect:/admin/account/index";
    }


}
