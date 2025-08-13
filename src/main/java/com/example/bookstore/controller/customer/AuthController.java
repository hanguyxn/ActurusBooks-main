package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.User;
import com.example.bookstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginForm() {
        return "customer/auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        Optional<User> userOpt = userService.login(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Chỉ dùng một session key duy nhất
            session.setAttribute("user", user);
            if ("admin".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/"; // User redirect về trang chủ
            }
        } else {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu");
            return "customer/auth/login";
        }
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "customer/auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            // Set default role
            user.setRole("customer");

            // Attempt to register user
            userService.register(user);

            // Success - redirect to login with success message
            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "customer/auth/login";

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Handle database constraint violations
            String errorMessage = e.getMessage();

            if (errorMessage.contains("email") || errorMessage.contains("Email")) {
                model.addAttribute("error", "Email này đã được sử dụng. Vui lòng chọn email khác.");
            } else if (errorMessage.contains("username") || errorMessage.contains("Tên đăng nhập")) {
                model.addAttribute("error", "Tên đăng nhập này đã được sử dụng. Vui lòng chọn tên khác.");
            } else {
                model.addAttribute("error", "Thông tin đăng ký đã tồn tại. Vui lòng kiểm tra lại.");
            }

            // Return to register form with error
            model.addAttribute("user", user);
            return "customer/auth/register";

        } catch (Exception e) {
            // Handle any other unexpected errors
            model.addAttribute("error", "Có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại.");
            model.addAttribute("user", user);
            return "customer/auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
