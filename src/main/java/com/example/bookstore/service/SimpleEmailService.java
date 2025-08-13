package com.example.bookstore.service;

import com.example.bookstore.entity.Orders;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class SimpleEmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ====================== PUBLIC METHODS ======================

    // Xác nhận đơn hàng COD
    public void sendOrderConfirmationEmail(Orders order) {
        try {
            String subject = "🎉 ActurusBooks - Xác nhận đơn hàng #" + order.getOrder_id();
            String preheader = "Đơn hàng của bạn đã được xác nhận. Chúng tôi sẽ giao trong 1–2 ngày làm việc.";
            String inner = buildOrderSummarySection(order,
                    "Đơn hàng của bạn đã được xác nhận ✅",
                    "Thanh toán khi nhận hàng (COD)",
                    "Đã xác nhận");

            String html = baseTemplate(preheader, "Xác nhận đơn hàng",
                    "Cảm ơn bạn đã đặt hàng tại ActurusBooks!", inner,
                    "Theo dõi đơn hàng", "#");

            sendHtml(order.getUser().getEmail(), subject, html);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email xác nhận: " + e.getMessage());
        }
    }

    // Thanh toán thành công (MoMo)
    public void sendPaymentSuccessEmail(Orders order) {
        try {
            String subject = "✅ ActurusBooks - Thanh toán thành công #" + order.getOrder_id();
            String preheader = "Thanh toán của bạn đã được xử lý thành công.";
            String inner = buildOrderSummarySection(order,
                    "Thanh toán thành công 🎉",
                    "MoMo",
                    "Đã thanh toán");

            String html = baseTemplate(preheader, "Thanh toán thành công",
                    "Cảm ơn bạn đã tin tưởng ActurusBooks!", inner,
                    "Xem đơn hàng", "#");

            sendHtml(order.getUser().getEmail(), subject, html);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email thanh toán: " + e.getMessage());
        }
    }

    // Hóa đơn
    public void sendInvoiceEmail(Orders order) {
        try {
            String subject = "📄 ActurusBooks - Hóa đơn #" + order.getOrder_id();
            String preheader = "Hóa đơn mua hàng từ ActurusBooks.";
            String inner = buildInvoiceSection(order);

            String html = baseTemplate(preheader, "Hóa đơn mua hàng",
                    "Đây là hóa đơn cho đơn hàng của bạn.", inner,
                    "Xem/ Tải hóa đơn", "#");

            sendHtml(order.getUser().getEmail(), subject, html);
        } catch (Exception e) {
            System.err.println("Lỗi gửi hóa đơn: " + e.getMessage());
        }
    }

    // ====================== SEND HTML ======================

    private void sendHtml(String to, String subject, String html) throws Exception {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom("vnxbypass@gmail.com", "ActurusBooks");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(mime);
    }

    // ====================== TEMPLATE BUILDER ======================

    private String baseTemplate(String preheader, String title, String intro,
            String innerContentHtml, String ctaText, String ctaUrl) {

        return """
                <!doctype html>
                <html lang="vi">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>ActurusBooks</title>
                  <style>
                    .preheader { display:none; visibility:hidden; opacity:0; color:transparent; height:0; width:0; overflow:hidden; mso-hide:all; }
                    body { margin:0; padding:0; background:#f6f7fb; font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
                    .container { max-width:640px; margin:0 auto; padding:24px 16px; }
                    .card { background:#ffffff; border-radius:16px; padding:28px; box-shadow:0 8px 24px rgba(26, 35, 126, 0.06); }
                    h1 { font-size:22px; margin:0 0 8px; color:#111827; }
                    p { color:#4b5563; line-height:1.6; margin:0 0 14px; }
                    .brand { font-weight:700; color:#1f2937; font-size:18px; }
                    .divider { height:1px; background:#e5e7eb; margin:20px 0; }
                    .table { width:100%%; border-collapse:collapse; margin-top:8px; }
                    .table th, .table td { text-align:left; padding:10px 8px; border-bottom:1px solid #f0f1f5; font-size:14px; }
                    .table th { color:#6b7280; font-weight:600; background:#fafafa; }
                    .badge { display:inline-block; padding:4px 10px; border-radius:9999px; background:#eef2ff; color:#3730a3; font-weight:600; font-size:12px; }
                    .cta-wrap { text-align:center; margin-top:22px; }
                    .cta { display:inline-block; padding:12px 18px; border-radius:12px; text-decoration:none; background:#4f46e5; color:#ffffff !important; font-weight:700; }
                    .footer { text-align:center; color:#9ca3af; font-size:12px; margin-top:16px; }
                    .head { display:flex; align-items:center; gap:10px; margin-bottom:8px; }
                    .logo { width:28px; height:28px; border-radius:8px; background:#4f46e5; display:inline-block; }
                  </style>
                </head>
                <body>
                  <span class="preheader">%s</span>
                  <div class="container">
                    <div class="card">
                      <div class="head">
                        <span class="logo"></span>
                        <span class="brand">ActurusBooks</span>
                      </div>
                      <h1>%s</h1>
                      <p>%s</p>
                      <div class="divider"></div>
                      %s
                      <div class="cta-wrap">
                        <a class="cta" href="%s" target="_blank" rel="noopener">%s</a>
                      </div>
                    </div>
                    <div class="footer">© %d ActurusBooks • 123 Đường Sách, Q.1, TP.HCM</div>
                  </div>
                </body>
                </html>
                """
                .formatted(
                        esc(preheader),
                        esc(title),
                        esc(intro),
                        innerContentHtml,
                        escAttr(ctaUrl),
                        esc(ctaText),
                        ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).getYear());
    }

    private String buildOrderSummarySection(Orders order, String heading, String paymentMethod, String status) {
        String nowVn = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy")
                .format(ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));

        return """
                <p><span class="badge">%s</span></p>
                <table class="table">
                  <tr><th>Mã đơn</th><td>#%d</td></tr>
                  <tr><th>Khách hàng</th><td>%s</td></tr>
                  <tr><th>Email</th><td>%s</td></tr>
                  <tr><th>Điện thoại</th><td>%s</td></tr>
                  <tr><th>Địa chỉ</th><td>%s</td></tr>
                  <tr><th>Hình thức</th><td>%s</td></tr>
                  <tr><th>Trạng thái</th><td>%s</td></tr>
                  <tr><th>Thời gian</th><td>%s</td></tr>
                  <tr><th>Tổng tiền</th><td><strong>%s</strong></td></tr>
                </table>
                """.formatted(
                esc(heading),
                order.getOrder_id(),
                esc(order.getUser().getUsername()),
                esc(order.getUser().getEmail()),
                esc(nvl(order.getPhone())),
                esc(nvl(order.getAddress())),
                esc(paymentMethod),
                esc(status),
                esc(nowVn),
                currencyVND(order.getTotal_amount()));
    }

    private String buildInvoiceSection(Orders order) {
        return """
                <p><span class="badge">🧾 HÓA ĐƠN BÁN HÀNG</span></p>
                <table class="table">
                  <tr><th>Mã đơn</th><td>#%d</td></tr>
                  <tr><th>Khách hàng</th><td>%s</td></tr>
                  <tr><th>Email</th><td>%s</td></tr>
                  <tr><th>Điện thoại</th><td>%s</td></tr>
                  <tr><th>Địa chỉ</th><td>%s</td></tr>
                  <tr><th>Tổng cộng</th><td><strong>%s</strong></td></tr>
                </table>
                """.formatted(
                order.getOrder_id(),
                esc(order.getUser().getUsername()),
                esc(order.getUser().getEmail()),
                esc(nvl(order.getPhone())),
                esc(nvl(order.getAddress())),
                currencyVND(order.getTotal_amount()));
    }

    // ====================== UTILS ======================

    private String currencyVND(Number amount) {
        if (amount == null)
            return "0 đ";
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(amount).replace("₫", "đ");
    }

    private String esc(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escAttr(String s) {
        return esc(s).replace("(", "%28").replace(")", "%29");
    }

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
