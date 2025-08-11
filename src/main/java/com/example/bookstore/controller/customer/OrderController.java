package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.CartItem;
import com.example.bookstore.entity.Orders;
import com.example.bookstore.entity.OrderDetail;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.Discount;
import com.example.bookstore.service.OrdersService;
import com.example.bookstore.service.OrderDetailService;
import com.example.bookstore.service.DiscountService;
import com.example.bookstore.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiscountService discountService;

    @PostMapping("/place")
    public String placeOrder(@RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String voucherCode,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Lấy giỏ hàng từ session
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cart");

        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
            return "redirect:/cart/view";
        }

        // Lấy user từ session (nếu đã đăng nhập)
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            // Yêu cầu đăng nhập
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đặt hàng!");
            return "redirect:/login";
        }

        try {
            // Tính tổng tiền chưa giảm giá
            double subtotal = cartItems.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();

            // Áp dụng mã giảm giá nếu có
            double discountAmount = 0;
            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                Discount discount = discountService.validateDiscount(voucherCode);
                if (discount != null) {
                    discountAmount = subtotal * discount.getValue() / 100;
                }
            }

            // Phí vận chuyển cố định
            double shippingFee = 20000;

            // Tổng tiền cuối cùng
            double totalAmount = subtotal - discountAmount + shippingFee;

            // Tạo đơn hàng
            Orders order = new Orders();
            order.setUser(currentUser);
            order.setPhone(phone);
            order.setAddress(address);
            order.setPayment_method(paymentMethod);
            order.setDiscount_code(voucherCode);
            order.setTotal_amount(BigDecimal.valueOf(totalAmount));
            order.setOrder_date(LocalDateTime.now());
            order.setStatus("Chờ xác nhận");

            // Lưu đơn hàng
            Orders savedOrder = ordersService.save(order);

            // Tạo chi tiết đơn hàng
            for (CartItem item : cartItems) {
                OrderDetail orderDetail = OrderDetail.builder()
                        .order(savedOrder)
                        .bookId(item.getBookId())
                        .quantity(item.getQuantity())
                        .price(BigDecimal.valueOf(item.getPrice()))
                        .build();
                orderDetailService.save(orderDetail);
            }

            if ("cod".equals(paymentMethod)) {
                // Thanh toán khi nhận hàng - chuyển thẳng đến trang thành công
                session.removeAttribute("cart");
                return "redirect:/order/success/" + savedOrder.getOrder_id();
            } else {
                // Thanh toán online - chuyển đến trang payment với orderId và amount
                session.setAttribute("tempOrderId", savedOrder.getOrder_id());
                session.setAttribute("tempAmount", BigDecimal.valueOf(totalAmount));

                if ("momo".equals(paymentMethod)) {
                    return "redirect:/payment/momo?orderId=" + savedOrder.getOrder_id() + "&amount="
                            + (long) totalAmount;
                } else if ("vnpay".equals(paymentMethod)) {
                    return "redirect:/payment/vnpay?orderId=" + savedOrder.getOrder_id() + "&amount="
                            + (long) totalAmount;
                } else if ("zalopay".equals(paymentMethod)) {
                    return "redirect:/payment/zalopay?orderId=" + savedOrder.getOrder_id() + "&amount="
                            + (long) totalAmount;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đặt hàng: " + e.getMessage());
            return "redirect:/cart/checkout";
        }

        return "redirect:/cart/checkout";
    }

    @GetMapping("/success/{orderId}")
    public String orderSuccess(@PathVariable Integer orderId, Model model) {
        Orders order = ordersService.getById(orderId).orElse(null);
        if (order != null) {
            List<OrderDetail> orderDetails = orderDetailService.getByOrderId(orderId);
            model.addAttribute("order", order);
            model.addAttribute("orderDetails", orderDetails);
        }
        return "customer/order/order-success";
    }

    @GetMapping("/history")
    public String orderHistory(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Orders> orders = ordersService.getByUserId(currentUser.getUser_id());
        model.addAttribute("orders", orders);
        return "customer/orders/orders";
    }

    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Integer orderId,
            HttpSession session,
            Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Orders order = ordersService.getById(orderId).orElse(null);
        if (order == null || !order.getUser().getUser_id().equals(currentUser.getUser_id())) {
            return "redirect:/user/orders";
        }

        List<OrderDetail> orderDetails = orderDetailService.getByOrderId(orderId);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        return "customer/orders/order-detail";
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Integer orderId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Orders order = ordersService.getById(orderId).orElse(null);
        if (order != null && order.getUser().getUser_id().equals(currentUser.getUser_id())
                && ("Chờ xác nhận".equals(order.getStatus()) || "Đã xác nhận".equals(order.getStatus()))) {
            order.setStatus("Đã hủy");
            ordersService.save(order);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy đơn hàng này!");
        }

        return "redirect:/user/orders";
    }
}
