package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.OrderDetail;
import com.example.bookstore.entity.Orders;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.OrdersRepository;
import com.example.bookstore.service.OrderDetailService;
import com.example.bookstore.service.OrdersService;
import com.example.bookstore.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserDashboardController {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/dashboard")
    public String userDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Thống kê cho user - sử dụng một phương thức nhất quán
        Integer userId = user.getUser_id();
        long totalOrders = ordersRepository.countByUserId(userId);
        long completedOrders = ordersRepository.findByUserId(userId)
                .stream().filter(o -> "Đã giao".equals(o.getStatus())).count();
        long pendingOrders = ordersRepository.findByUserId(userId)
                .stream()
                .filter(o -> "Chờ xác nhận".equals(o.getStatus()) || "Đã xác nhận".equals(o.getStatus())
                        || "Đang giao".equals(o.getStatus()))
                .count();
        long wishlistCount = wishlistService.countByUserId(userId);

        model.addAttribute("user", user);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("wishlistCount", wishlistCount);

        return "customer/dashboard";
    }

    @GetMapping("/profile")
    public String userProfile(HttpSession session, Model model) {
        // Redirect đến dashboard - chỉ dùng 1 layout
        return "redirect:/user/dashboard";
    }

    @GetMapping("/orders")
    public String userOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy danh sách orders của user
        model.addAttribute("user", user);
        model.addAttribute("orders", ordersService.getByUserId(user.getUser_id()));

        return "customer/orders/orders";
    }

    @GetMapping("/orders/detail/{orderId}")
    public String userOrderDetail(@PathVariable Integer orderId,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Orders order = ordersService.getById(orderId).orElse(null);
        if (order == null || !order.getUser().getUser_id().equals(user.getUser_id())) {
            return "redirect:/user/orders";
        }

        List<OrderDetail> orderDetails = orderDetailService.getByOrderId(orderId);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        return "customer/orders/order-detail";
    }
}
