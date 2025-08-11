package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.Orders;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.OrdersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private OrdersService ordersService;

    // MoMo Test Environment Configuration
    private static final String MOMO_PARTNER_CODE = "MOMO";
    private static final String MOMO_ACCESS_KEY = "F8BBA842ECF85";
    private static final String MOMO_SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private static final String MOMO_ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";
    private static final String RETURN_URL = "http://localhost:8080/payment/momo/return";
    private static final String NOTIFY_URL = "http://localhost:8080/payment/momo/notify";

    @GetMapping("/test")
    public String testPage() {
        return "payment/momo-test";
    }

    @GetMapping("/momo")
    public String momoPayment(@RequestParam Integer orderId,
            @RequestParam Long amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy user từ session
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                currentUser = (User) session.getAttribute("currentUser");
            }
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
                return "redirect:/login";
            }

            // Lấy thông tin đơn hàng
            Orders order = ordersService.getById(orderId).orElse(null);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
                return "redirect:/cart/checkout";
            }

            // Tạo request đến MoMo
            String requestId = UUID.randomUUID().toString();
            String momoOrderId = "ORDER_" + orderId + "_" + System.currentTimeMillis(); // Tạo unique orderId cho MoMo
            String orderInfo = "Thanh toán đơn hàng #" + orderId;
            String extraData = "";
            String requestType = "captureWallet";

            // Tạo signature
            String rawSignature = "accessKey=" + MOMO_ACCESS_KEY +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + NOTIFY_URL +
                    "&orderId=" + momoOrderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + MOMO_PARTNER_CODE +
                    "&redirectUrl=" + RETURN_URL +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            String signature = hmacSHA256(rawSignature, MOMO_SECRET_KEY);

            // Tạo JSON request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", MOMO_PARTNER_CODE);
            requestBody.put("accessKey", MOMO_ACCESS_KEY);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", momoOrderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", RETURN_URL);
            requestBody.put("ipnUrl", NOTIFY_URL);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType);
            requestBody.put("signature", signature);

            // Gửi request đến MoMo
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            URL url = new URL(MOMO_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
                writer.write(jsonRequest);
                writer.flush();
            }

            // Đọc response
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse response
            Map<String, Object> responseData = objectMapper.readValue(response.toString(), Map.class);

            if (responseData.get("resultCode").equals(0)) {
                String payUrl = (String) responseData.get("payUrl");
                return "redirect:" + payUrl;
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Lỗi tạo thanh toán MoMo: " + responseData.get("message"));
                return "redirect:/cart/checkout";
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tạo thanh toán MoMo: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }

    @GetMapping("/momo/return")
    public String momoReturn(@RequestParam Map<String, String> params,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            String resultCode = params.get("resultCode");
            String momoOrderId = params.get("orderId");
            String message = params.get("message");

            // Extract original orderId from momoOrderId
            String originalOrderId = extractOriginalOrderId(momoOrderId);

            if ("0".equals(resultCode)) {
                // Thanh toán thành công
                session.removeAttribute("cart");
                return "redirect:/order/success/" + originalOrderId;
            } else {
                // Thanh toán thất bại
                redirectAttributes.addFlashAttribute("error", "Thanh toán MoMo thất bại: " + message);
                return "redirect:/cart/checkout";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }

    @PostMapping("/momo/notify")
    @ResponseBody
    public String momoNotify(@RequestBody Map<String, Object> params) {
        try {
            String resultCode = params.get("resultCode").toString();
            String momoOrderId = params.get("orderId").toString();

            // Extract original orderId from momoOrderId
            String originalOrderId = extractOriginalOrderId(momoOrderId);

            if ("0".equals(resultCode)) {
                // Cập nhật trạng thái đơn hàng
                Orders order = ordersService.getById(Integer.parseInt(originalOrderId)).orElse(null);
                if (order != null) {
                    order.setStatus("Đã thanh toán");
                    ordersService.save(order);
                }
            }

            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    // MoMo Payment Simulator endpoints
    @GetMapping("/momo/simulate")
    public String momoSimulatePage(@RequestParam Integer orderId,
            @RequestParam Long amount,
            Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        return "payment/momo-simulate";
    }

    @PostMapping("/momo/simulate")
    public String momoSimulatePayment(@RequestParam String orderId,
            @RequestParam String resultCode,
            @RequestParam String message,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            // Extract original orderId if it's in MoMo format
            String originalOrderId = extractOriginalOrderId(orderId);

            if ("0".equals(resultCode)) {
                // Simulate successful payment
                Orders order = ordersService.getById(Integer.parseInt(originalOrderId)).orElse(null);
                if (order != null) {
                    order.setStatus("Đã thanh toán");
                    ordersService.save(order);
                }

                session.removeAttribute("cart");
                return "redirect:/order/success/" + originalOrderId;
            } else {
                // Simulate failed payment
                redirectAttributes.addFlashAttribute("error", "Thanh toán MoMo thất bại: " + message);
                return "redirect:/cart/checkout";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }

    @GetMapping("/vnpay")
    public String vnpayPayment(@RequestParam Integer orderId,
            @RequestParam Long amount,
            RedirectAttributes redirectAttributes) {
        // VNPay implementation placeholder
        redirectAttributes.addFlashAttribute("error", "VNPay chưa được cấu hình!");
        return "redirect:/cart/checkout";
    }

    @GetMapping("/zalopay")
    public String zalopayPayment(@RequestParam Integer orderId,
            @RequestParam Long amount,
            RedirectAttributes redirectAttributes) {
        // ZaloPay implementation placeholder
        redirectAttributes.addFlashAttribute("error", "ZaloPay chưa được cấu hình!");
        return "redirect:/cart/checkout";
    }

    // Utility method để tạo HMAC SHA256 signature
    private String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Extract original orderId from MoMo orderId (format: ORDER_123_1234567890)
    private String extractOriginalOrderId(String momoOrderId) {
        if (momoOrderId != null && momoOrderId.startsWith("ORDER_")) {
            String[] parts = momoOrderId.split("_");
            if (parts.length >= 2) {
                return parts[1]; // Return the original orderId part
            }
        }
        return momoOrderId; // Return as-is if format doesn't match
    }
}
