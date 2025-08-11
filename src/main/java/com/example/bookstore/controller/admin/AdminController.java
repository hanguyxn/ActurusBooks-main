package com.example.bookstore.controller.admin;

import com.example.bookstore.service.BookService;
import com.example.bookstore.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private BookService bookService;

    @Autowired
    private OrdersService ordersService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Basic stats
            long totalBooks = bookService.findAll().size();
            long totalOrders = ordersService.getAll().size();

            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalUsers", 100);
            model.addAttribute("totalRevenue", "25,350,000đ");

            // Recent orders (top 5)
            var allOrders = ordersService.getAll();
            var recentOrders = allOrders.size() > 5 ? allOrders.subList(0, 5) : allOrders;
            model.addAttribute("recentOrders", recentOrders);

            System.out.println("Dashboard loaded - Books: " + totalBooks + ", Orders: " + totalOrders);

            return "admin/dashboard";
        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();

            // Set default values in case of error
            model.addAttribute("totalBooks", 0);
            model.addAttribute("totalOrders", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalRevenue", "0đ");
            model.addAttribute("recentOrders", List.of());

            return "admin/dashboard";
        }
    }
}