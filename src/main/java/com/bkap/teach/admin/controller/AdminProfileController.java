package com.bkap.teach.admin.controller;

import com.bkap.teach.admin.dto.request.AdminChangePasswordRequest;
import com.bkap.teach.admin.dto.request.AdminProfileRequest;
import com.bkap.teach.admin.service.AdminProfileService;
import com.bkap.teach.audit.AuditAnotation;
import com.bkap.teach.common.notify.Notify;
import com.bkap.teach.entity.User;
import com.bkap.teach.enums.Action;
import com.bkap.teach.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin/profile")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class AdminProfileController {

    @Autowired
    private AdminProfileService adminProfileService;

    @GetMapping
    public String showProfile(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User user = adminProfileService.getCurrentUser(principal.getId());
        model.addAttribute("user", user);
        return "admin/profile/index";
    }


    @GetMapping("/edit")
    public String showEditForm(Model model, @AuthenticationPrincipal UserPrincipal principal) {

        User user = adminProfileService.getCurrentUser(principal.getId());

        AdminProfileRequest dto = new AdminProfileRequest(
                user.getFullname(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getAvatar()
        );

        model.addAttribute("adminProfileRequest", dto);
        model.addAttribute("user", user);
        return "admin/profile/edit";
    }

    @AuditAnotation(
            action = Action.UPDATE,
            objectType = "ADMIN_PROFILE"
    )
    @PostMapping("/edit")
    public String updateProfile(
            @Valid @ModelAttribute("adminProfileRequest") AdminProfileRequest dto,
            BindingResult result,
            @RequestParam("avatarFile") MultipartFile avatarFile,
            @AuthenticationPrincipal UserPrincipal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            User user = adminProfileService.getCurrentUser(principal.getId());
            model.addAttribute("user", user);
            Notify.warning(model, "Vui lòng kiểm tra lại thông tin hồ sơ!");
            return "admin/profile/edit";
        }

        try {
            adminProfileService.updateProfile(
                    principal.getId(),
                    dto,
                    avatarFile
            );
        } catch (IllegalArgumentException e) {
            User user = adminProfileService.getCurrentUser(principal.getId());
            model.addAttribute("user", user);

            Notify.error(model, e.getMessage());
            return "admin/profile/edit";
        }

        Notify.success(redirectAttributes, "Cập nhật thông tin hồ sơ thành công!");
        return "redirect:/admin/profile";
    }


    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("adminChangePasswordRequest", new AdminChangePasswordRequest());
        return "admin/profile/change-password";
    }

    @AuditAnotation(
            action = Action.UPDATE,
            objectType = "ADMIN_UPDATE_PASSWORD"
    )
    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("adminChangePasswordRequest") AdminChangePasswordRequest dto,
            BindingResult result,
            @AuthenticationPrincipal UserPrincipal principal,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            Notify.warning(model, "Vui lòng điền đầy đủ và đúng thông tin mật khẩu !");
            return "admin/profile/change-password";
        }

        try {
            adminProfileService.changePassword(
                    principal.getId(),
                    dto
            );
        } catch (IllegalArgumentException e) {
            Notify.error(model, e.getMessage());
            return "admin/profile/change-password";
        }

        request.getSession().invalidate();
        Notify.success(redirectAttributes, "Đổi mật khẩu thành công ! Vui lòng đăng nhập lại.");
        return "redirect:/admin/auth/login";
    }

}