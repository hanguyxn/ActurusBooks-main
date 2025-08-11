package com.example.bookstore.controller.admin;

import com.example.bookstore.entity.Orders;
import com.example.bookstore.enums.OrderStatus;
import com.example.bookstore.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrdersService ordersService;

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", ordersService.getAll());
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "admin/orders/orders";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderDetails(@PathVariable Integer id) {
        try {
            Optional<Orders> orderOpt = ordersService.getById(id);
            if (orderOpt.isPresent()) {
                Orders order = orderOpt.get();

                // Manually create response map to match template expectations
                Map<String, Object> response = new HashMap<>();
                response.put("order_id", order.getOrder_id());
                response.put("order_date", order.getOrder_date());
                response.put("total_amount", order.getTotal_amount());
                response.put("phone", order.getPhone());
                response.put("address", order.getAddress());
                response.put("payment_method", order.getPayment_method());
                response.put("status", order.getStatus());
                response.put("discount_code", order.getDiscount_code());

                // User info
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("full_name", order.getUser().getFull_name());
                userInfo.put("email", order.getUser().getEmail());
                response.put("user", userInfo);

                // Order items - manually load to avoid lazy loading issues
                if (order.getOrderItems() != null) {
                    response.put("orderItems", order.getOrderItems().stream()
                            .map(item -> {
                                Map<String, Object> itemMap = new HashMap<>();
                                itemMap.put("quantity", item.getQuantity());
                                itemMap.put("price", item.getPrice());

                                // Book info - manually set to avoid proxy issues
                                Map<String, Object> bookMap = new HashMap<>();
                                if (item.getBook() != null) {
                                    bookMap.put("title", item.getBook().getTitle());
                                    bookMap.put("image_url", item.getBook().getImageUrl());
                                }
                                itemMap.put("book", bookMap);
                                return itemMap;
                            })
                            .toList());
                }

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("Error getting order details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        try {
            Optional<Orders> orderOpt = ordersService.getById(id);
            if (orderOpt.isPresent()) {
                Orders order = orderOpt.get();
                order.setStatus(status);
                ordersService.save(order);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Cập nhật trạng thái đơn hàng thành công");
                response.put("newStatus", status);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi cập nhật trạng thái: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Integer id) {
        try {
            ordersService.delete(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa đơn hàng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi xóa đơn hàng: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
