package com.example.bookstore.config;

import com.example.bookstore.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();

        // Routes yêu cầu đăng nhập
        if (requestURI.startsWith("/user/") ||
                requestURI.startsWith("/wishlist") ||
                requestURI.startsWith("/order/") ||
                requestURI.equals("/checkout")) {

            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");

            if (user == null) {
                response.sendRedirect("/login?redirect=" + requestURI);
                return false;
            }
        }

        // Routes chỉ admin
        if (requestURI.startsWith("/admin/") && !requestURI.equals("/admin/login")) {
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");

            if (user == null || !"admin".equalsIgnoreCase(user.getRole())) {
                response.sendRedirect("/admin/login");
                return false;
            }
        }

        return true;
    }
}
