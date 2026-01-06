package com.bkap.teach.admin.controller;

import com.bkap.teach.admin.dto.request.AdminLoginRequest;
import com.bkap.teach.admin.dto.request.AdminRegisterRequest;
import com.bkap.teach.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.bkap.teach.common.notify.Notify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/auth")
public class AdminAuthController {

    @Autowired
    private UserService userService;


    @GetMapping("/login")
    public String loginAdmin(HttpServletRequest request, Model model) {
        model.addAttribute("adminLoginRequest", new AdminLoginRequest());
        Object notify = request.getSession().getAttribute("notify");
        if (notify != null) {
            model.addAttribute("notify", notify);
            request.getSession().removeAttribute("notify");
        }

        return "admin/auth/login";
    }


    @GetMapping("/register")
    public String registerAdmin(Model model) {
        model.addAttribute("adminRegisterRequest", new AdminRegisterRequest());
        return "admin/auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("adminRegisterRequest") AdminRegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes ra,
            HttpServletRequest httpRequest) {
        if (result.hasErrors()) {
            return "admin/auth/register";
        }

        try {
            userService.registerAdmin(request, httpRequest);
        } catch (RuntimeException e) {
            Notify.error(model, e.getMessage());
            return "admin/auth/register";
        }

        Notify.success(ra, "Đăng ký thành công! Bạn có thể đăng nhập.");
        return "redirect:/admin/auth/login";
    }

}
