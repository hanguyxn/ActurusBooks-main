package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String userProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // ✅ Admin redirect về admin dashboard
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        // ✅ User redirect về user dashboard - chỉ dùng 1 layout
        return "redirect:/user/dashboard";
    }
}
