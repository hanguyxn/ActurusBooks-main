package com.example.bookstore.controller.admin;

import com.example.bookstore.entity.Notification;
import com.example.bookstore.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/notifications")
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String adminNotifications(Model model) {
        model.addAttribute("notifications", notificationService.findAll());
        model.addAttribute("newNotification", new Notification());
        return "admin/notification/index";
    }

    @PostMapping("/add")
    public String addNotification(Notification notification) {
        notificationService.save(notification);
        return "redirect:/admin/notifications";
    }

    @GetMapping("/edit/{id}")
    public String editNotification(@PathVariable Integer id, Model model) {
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        model.addAttribute("notification", notification);
        return "admin/notification/index"; // Sử dụng cùng template, thêm form sửa
    }

    @PostMapping("/update")
    public String updateNotification(Notification notification) {
        notificationService.save(notification);
        return "redirect:/admin/notifications";
    }

    @GetMapping("/delete/{id}")
    public String deleteNotification(@PathVariable Integer id) {
        notificationService.deleteById(id);
        return "redirect:/admin/notifications";
    }
}