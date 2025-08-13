package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.CartItem;
import com.example.bookstore.entity.Orders;
import com.example.bookstore.entity.OrderDetail;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.UserAddress;
import com.example.bookstore.entity.Discount;
import com.example.bookstore.service.OrdersService;
import com.example.bookstore.service.OrderDetailService;
import com.example.bookstore.service.DiscountService;
import com.example.bookstore.service.InvoiceService;
import com.example.bookstore.service.UserAddressService;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.SimpleEmailService;
import com.example.bookstore.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
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
    private BookService bookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private SimpleEmailService emailService;

    @PostMapping("/place")
    public String placeOrder(@RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String paymentMethod,
            @RequestParam(defaultValue = "standard") String shippingMethod,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String voucherCode,
            @RequestParam(required = false) Integer addressId, // ID của địa chỉ đã chọn
            @RequestParam(required = false) String newFullName, // Tên cho địa chỉ mới
            @RequestParam(required = false) String newPhone, // SĐT cho địa chỉ mới
            @RequestParam(required = false) String provinceCode,
            @RequestParam(required = false) String provinceName,
            @RequestParam(required = false) String wardCode,
            @RequestParam(required = false) String wardName,
            @RequestParam(required = false) String streetAddress,
            @RequestParam(required = false, defaultValue = "false") Boolean saveAddress, // Có lưu địa chỉ mới không
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
            System.out.println("=== Order Placement Debug ===");
            System.out.println("Payment method: " + paymentMethod);
            System.out.println("Shipping method: " + shippingMethod);
            System.out.println("Address ID: " + addressId);
            System.out.println("Voucher code: " + voucherCode);
            System.out.println("Cart items count: " + (cartItems != null ? cartItems.size() : 0));

            // Xử lý địa chỉ giao hàng
            String finalAddress = address;
            String finalFullName = fullName;
            String finalPhone = phone;

            // Nếu user chọn địa chỉ từ danh sách có sẵn
            if (addressId != null && addressId > 0) {
                Optional<UserAddress> selectedAddress = userAddressService.getById(addressId);
                if (selectedAddress.isPresent()) {
                    UserAddress addr = selectedAddress.get();
                    finalFullName = addr.getFullName();
                    finalPhone = addr.getPhone();
                    finalAddress = addr.getFullAddress();
                }
            }
            // Nếu user nhập địa chỉ mới và muốn lưu
            else if (saveAddress && provinceCode != null && wardCode != null && streetAddress != null) {
                finalFullName = newFullName != null ? newFullName : fullName;
                finalPhone = newPhone != null ? newPhone : phone;
                finalAddress = streetAddress + ", " + wardName + ", " + provinceName;

                // Lưu địa chỉ mới vào database
                UserAddress newAddress = UserAddress.builder()
                        .user(currentUser)
                        .fullName(finalFullName)
                        .phone(finalPhone)
                        .provinceCode(provinceCode)
                        .provinceName(provinceName)
                        .wardCode(wardCode)
                        .wardName(wardName)
                        .streetAddress(streetAddress)
                        .isDefault(false) // Không tự động set làm mặc định
                        .build();

                userAddressService.save(newAddress);
            }

            // Tính tổng tiền chưa giảm giá
            double subtotal = cartItems.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();

            // Kiểm tra tồn kho cho tất cả sản phẩm trong giỏ hàng trước khi đặt hàng
            for (CartItem item : cartItems) {
                if (!bookService.hasStock(item.getBookId(), item.getQuantity())) {
                    redirectAttributes.addFlashAttribute("error",
                            "Sản phẩm '" + item.getTitle() + "' không đủ hàng tồn kho!");
                    return "redirect:/cart/checkout";
                }
            }

            // Áp dụng mã giảm giá từ session (đã được validate trước đó)
            double discountAmount = 0;
            String sessionVoucherCode = (String) session.getAttribute("voucherCode");
            Double sessionDiscountAmount = (Double) session.getAttribute("discountAmount");

            if (sessionVoucherCode != null && sessionDiscountAmount != null) {
                voucherCode = sessionVoucherCode;
                discountAmount = sessionDiscountAmount;
            } else if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                // Fallback: validate từ parameter nếu không có trong session
                Discount discount = discountService.validateDiscount(voucherCode);
                if (discount != null) {
                    discountAmount = subtotal * discount.getValue() / 100;
                }
            }

            // Phí vận chuyển theo phương thức được chọn
            double shippingFee = 20000; // default standard
            if (shippingMethod != null) {
                switch (shippingMethod.toLowerCase()) {
                    case "express":
                        shippingFee = 35000;
                        break;
                    case "same_day":
                        shippingFee = 50000;
                        break;
                    case "standard":
                    default:
                        shippingFee = 20000; // standard
                }
            }

            System.out.println("Shipping method: " + shippingMethod + ", fee: " + shippingFee);

            // Tổng tiền cuối cùng
            double totalAmount = subtotal - discountAmount + shippingFee;

            // Tạo mã hóa đơn unique
            String invoiceNumber = generateInvoiceNumber();

            // Tạo đơn hàng
            Orders order = new Orders();
            order.setUser(currentUser);
            order.setPhone(finalPhone);
            order.setAddress(finalAddress);
            order.setPayment_method(paymentMethod);
            order.setDiscount_code(voucherCode);
            order.setTotal_amount(BigDecimal.valueOf(totalAmount));
            order.setOrder_date(LocalDateTime.now());
            order.setStatus("Chờ xác nhận");

            // Set payment status based on payment method
            if ("cod".equals(paymentMethod)) {
                order.setPayment_status("Chưa thanh toán"); // COD - thanh toán khi nhận hàng
            } else {
                order.setPayment_status("Chưa thanh toán"); // Online payment - chưa thanh toán
            }

            // Lưu chi tiết chi phí cho hóa đơn
            order.setSubtotal(BigDecimal.valueOf(subtotal));
            order.setDiscount_amount(BigDecimal.valueOf(discountAmount));
            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                Discount discount = discountService.validateDiscount(voucherCode);
                if (discount != null) {
                    order.setDiscount_percentage(discount.getValue());
                }
            }
            order.setShipping_fee(BigDecimal.valueOf(shippingFee));
            order.setShipping_method(getShippingMethodName(shippingMethod));
            order.setInvoice_number(invoiceNumber);

            if ("cod".equals(paymentMethod)) {
                // Thanh toán khi nhận hàng - tạo đơn hàng ngay
                Orders savedOrder = ordersService.save(order);

                // Tạo chi tiết đơn hàng và trừ stock
                for (CartItem item : cartItems) {
                    OrderDetail orderDetail = OrderDetail.builder()
                            .order(savedOrder)
                            .bookId(item.getBookId())
                            .quantity(item.getQuantity())
                            .price(BigDecimal.valueOf(item.getPrice()))
                            .build();
                    orderDetailService.save(orderDetail);

                    // Trừ tồn kho
                    bookService.reduceStock(item.getBookId(), item.getQuantity());
                }

                // Clear cart only after successful order creation
                session.removeAttribute("cart");

                // Clear discount information from session after successful order
                session.removeAttribute("voucherCode");
                session.removeAttribute("discountValue");
                session.removeAttribute("discountAmount");

                // Gửi email xác nhận đơn hàng COD
                try {
                    emailService.sendOrderConfirmationEmail(savedOrder);
                } catch (Exception e) {
                    System.err.println("Failed to send order confirmation email: " + e.getMessage());
                }

                return "redirect:/order/success/" + savedOrder.getOrder_id();
            } else {
                // Thanh toán online - tạo đơn hàng ngay và chuyển đến thanh toán
                Orders savedOrder = ordersService.save(order);

                // Tạo chi tiết đơn hàng ngay
                for (CartItem item : cartItems) {
                    OrderDetail orderDetail = OrderDetail.builder()
                            .order(savedOrder)
                            .bookId(item.getBookId())
                            .quantity(item.getQuantity())
                            .price(BigDecimal.valueOf(item.getPrice()))
                            .build();
                    orderDetailService.save(orderDetail);

                    // Trừ tồn kho ngay
                    bookService.reduceStock(item.getBookId(), item.getQuantity());
                }

                // Clear cart after creating order
                session.removeAttribute("cart");

                // Clear discount information from session
                session.removeAttribute("voucherCode");
                session.removeAttribute("discountValue");
                session.removeAttribute("discountAmount");
                session.setAttribute("tempAmount", BigDecimal.valueOf(totalAmount));

                if ("momo".equals(paymentMethod)) {
                    return "redirect:/payment/momo?orderId=" + savedOrder.getOrder_id() + "&amount="
                            + (long) totalAmount;
                } else if ("zalopay".equals(paymentMethod)) {
                    return "redirect:/payment/zalopay?orderId=" + savedOrder.getOrder_id() + "&amount="
                            + (long) totalAmount;
                }
                // Remove VNPay as requested
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

            // Phục hồi tồn kho khi hủy đơn hàng
            List<OrderDetail> orderDetails = orderDetailService.getByOrderId(orderId);
            for (OrderDetail detail : orderDetails) {
                bookService.increaseStock(detail.getBookId(), detail.getQuantity());
            }

            order.setStatus("Đã hủy");
            ordersService.save(order);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy đơn hàng này!");
        }

        return "redirect:/user/orders";
    }

    // Method to handle successful payment
    @GetMapping("/payment-success")
    public String handlePaymentSuccess(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Orders tempOrder = (Orders) session.getAttribute("tempOrder");
            @SuppressWarnings("unchecked")
            List<CartItem> tempCartItems = (List<CartItem>) session.getAttribute("tempCartItems");

            if (tempOrder == null || tempCartItems == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin đơn hàng!");
                return "redirect:/cart/view";
            }

            // Save the order now that payment is successful
            Orders savedOrder = ordersService.save(tempOrder);

            // Create order details and reduce stock
            for (CartItem item : tempCartItems) {
                OrderDetail orderDetail = OrderDetail.builder()
                        .order(savedOrder)
                        .bookId(item.getBookId())
                        .quantity(item.getQuantity())
                        .price(BigDecimal.valueOf(item.getPrice()))
                        .build();
                orderDetailService.save(orderDetail);

                // Trừ tồn kho
                bookService.reduceStock(item.getBookId(), item.getQuantity());
            }

            // Clear session data
            session.removeAttribute("tempOrder");
            session.removeAttribute("tempCartItems");
            session.removeAttribute("tempAmount");
            session.removeAttribute("cart");

            return "redirect:/order/success/" + savedOrder.getOrder_id();

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xử lý đơn hàng!");
            return "redirect:/cart/checkout";
        }
    }

    // Helper method to generate invoice number
    private String generateInvoiceNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        String randomSuffix = String.format("%04d", (int) (Math.random() * 10000));
        return "INV" + dateStr + randomSuffix;
    }

    // Helper method to get shipping method name
    private String getShippingMethodName(String method) {
        if (method == null)
            return "Tiêu chuẩn";

        switch (method.toLowerCase()) {
            case "standard":
                return "Tiêu chuẩn";
            case "express":
                return "Nhanh";
            case "same_day":
                return "Trong ngày";
            default:
                return "Tiêu chuẩn";
        }
    }

    // Method to handle failed payment
    @GetMapping("/payment-failed")
    public String handlePaymentFailed(HttpSession session, RedirectAttributes redirectAttributes) {
        // Clear temp data but keep cart
        session.removeAttribute("tempOrder");
        session.removeAttribute("tempCartItems");
        session.removeAttribute("tempAmount");

        redirectAttributes.addFlashAttribute("error", "Thanh toán không thành công. Vui lòng thử lại!");
        return "redirect:/cart/checkout";
    }

    // Download invoice PDF
    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Integer orderId, HttpSession session) {
        try {
            // Check if user is logged in
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            // Get order and verify ownership
            Optional<Orders> orderOpt = ordersService.getById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(404).build();
            }

            Orders order = orderOpt.get();
            if (!order.getUser().getUser_id().equals(user.getUser_id())) {
                return ResponseEntity.status(403).build();
            }

            // Generate PDF
            byte[] pdfBytes = invoiceService.generateInvoicePDF(order);
            String filename = invoiceService.getInvoiceFilename(order);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
