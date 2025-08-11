package com.example.bookstore.controller.customer;

import com.example.bookstore.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String clientNotifications(Model model) {
        model.addAttribute("notifications", notificationService.findAllDTOs());
        return "customer/notification/notification"; // đúng với ảnh bạn show
    }
}